import { useState, useMemo, useCallback } from "react";
import {
  DndContext,
  DragOverlay,
  closestCorners,
  PointerSensor,
  KeyboardSensor,
  useSensor,
  useSensors,
  type DragStartEvent,
  type DragEndEvent,
} from "@dnd-kit/core";
import { useDroppable, useDraggable } from "@dnd-kit/core";
import { cn } from "../../lib/utils";
import { formatDate } from "../../lib/formatters";
import { PRIORITY_COLORS } from "../../lib/constants";
import type { Task } from "../../types";

const COLUMNS = [
  { id: "OPEN", title: "Open", dotColor: "bg-teal-500", headerColor: "text-teal-700", borderColor: "border-teal-200", bgHover: "bg-teal-50/50" },
  { id: "IN_PROGRESS", title: "In Progress", dotColor: "bg-blue-500", headerColor: "text-blue-700", borderColor: "border-blue-200", bgHover: "bg-blue-50/50" },
  { id: "DONE", title: "Done", dotColor: "bg-slate-400", headerColor: "text-slate-600", borderColor: "border-slate-200", bgHover: "bg-slate-50/50" },
] as const;

interface KanbanBoardProps {
  tasks: Task[];
  onStatusChange: (id: number, status: "OPEN" | "IN_PROGRESS" | "DONE") => void;
  onTaskClick: (task: Task) => void;
}

export default function KanbanBoard({ tasks, onStatusChange, onTaskClick }: KanbanBoardProps) {
  const [activeTask, setActiveTask] = useState<Task | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor)
  );

  const tasksByStatus = useMemo<Record<string, Task[]>>(() => ({
    OPEN: tasks.filter((t) => t.status === "OPEN"),
    IN_PROGRESS: tasks.filter((t) => t.status === "IN_PROGRESS"),
    DONE: tasks.filter((t) => t.status === "DONE"),
  }), [tasks]);

  const handleDragStart = useCallback((event: DragStartEvent) => {
    const task = tasks.find((t) => t.id === event.active.id);
    setActiveTask(task ?? null);
  }, [tasks]);

  const handleDragEnd = useCallback((event: DragEndEvent) => {
    setActiveTask(null);
    const { active, over } = event;
    if (!over) return;

    const taskId = active.id as number;
    const newStatus = over.id as string;
    const task = tasks.find((t) => t.id === taskId);

    if (task && task.status !== newStatus && COLUMNS.some((c) => c.id === newStatus)) {
      onStatusChange(taskId, newStatus as "OPEN" | "IN_PROGRESS" | "DONE");
    }
  }, [tasks, onStatusChange]);

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCorners}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
    >
      <div className="overflow-auto max-h-[calc(100vh-220px)]">
        <div className="inline-grid grid-cols-3 gap-3 min-w-[720px] w-full">
          {COLUMNS.map((col) => (
            <KanbanColumn
              key={col.id}
              column={col}
              tasks={tasksByStatus[col.id] ?? []}
              onTaskClick={onTaskClick}
            />
          ))}
        </div>
      </div>

      <DragOverlay>
        {activeTask && <KanbanCardOverlay task={activeTask} />}
      </DragOverlay>
    </DndContext>
  );
}

function KanbanColumn({
  column,
  tasks,
  onTaskClick,
}: {
  column: (typeof COLUMNS)[number];
  tasks: Task[];
  onTaskClick: (task: Task) => void;
}) {
  const { setNodeRef, isOver } = useDroppable({ id: column.id });

  return (
    <div
      ref={setNodeRef}
      className={cn(
        "rounded-xl border-2 border-dashed p-2.5 min-h-[400px] transition-colors",
        isOver ? cn(column.borderColor, column.bgHover) : "border-border bg-slate-50/30"
      )}
    >
      <div className="flex items-center gap-2 mb-2 px-1">
        <span className={cn("w-2.5 h-2.5 rounded-full", column.dotColor)} />
        <h3 className={cn("text-sm font-semibold", column.headerColor)}>{column.title}</h3>
        <span className="text-xs text-text-muted bg-white rounded-full px-2 py-0.5 border border-border">
          {tasks.length}
        </span>
      </div>

      <div className="space-y-1.5">
        {tasks.map((task) => (
          <KanbanCard key={task.id} task={task} onTaskClick={onTaskClick} />
        ))}
        {tasks.length === 0 && (
          <p className="text-xs text-text-muted text-center py-4 italic">
            No tasks
          </p>
        )}
      </div>
    </div>
  );
}

function KanbanCard({
  task,
  onTaskClick,
}: {
  task: Task;
  onTaskClick: (task: Task) => void;
}) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: task.id,
  });

  const style = transform
    ? { transform: `translate(${transform.x}px, ${transform.y}px)` }
    : undefined;

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...listeners}
      {...attributes}
      onClick={() => onTaskClick(task)}
      className={cn(
        "bg-white rounded-lg border border-border border-l-4 p-2.5 cursor-grab active:cursor-grabbing",
        "hover:shadow-md transition-shadow",
        PRIORITY_COLORS[task.priority]?.borderL ?? PRIORITY_COLORS.LOW.borderL,
        isDragging && "opacity-30",
        task.overdue && task.status !== "DONE" && "bg-red-50"
      )}
    >
      <p className={cn(
        "text-sm font-medium text-text-primary",
        task.status === "DONE" && "line-through opacity-60"
      )}>
        {task.title}
      </p>

      <div className="flex items-center justify-between mt-1.5">
        <span className="text-xs text-text-muted">
          {task.assignedToName}
        </span>
        <span className={cn(
          "text-xs font-medium whitespace-nowrap",
          task.overdue && task.status !== "DONE" ? "text-danger" : "text-text-muted"
        )}>
          {formatDate(task.dueDate)}
        </span>
      </div>

      {task.dealName && (
        <p className="text-xs text-text-muted mt-1">
          {task.dealName}
        </p>
      )}

      {task.overdue && task.status !== "DONE" && (
        <span className="inline-block text-[10px] font-medium px-1.5 py-0.5 rounded-full bg-red-100 text-red-700 mt-1">
          OVERDUE
        </span>
      )}
    </div>
  );
}

function KanbanCardOverlay({ task }: { task: Task }) {
  return (
    <div className={cn(
      "bg-white rounded-lg border border-border border-l-4 p-3 shadow-xl w-[280px]",
      PRIORITY_COLORS[task.priority]?.borderL ?? PRIORITY_COLORS.LOW.borderL
    )}>
      <p className="text-sm font-medium text-text-primary line-clamp-2">{task.title}</p>
      <div className="flex items-center justify-between mt-2">
        <span className="text-[10px] text-text-muted">{task.assignedToName?.split(" ")[0]}</span>
        <span className="text-[10px] text-text-muted">{formatDate(task.dueDate)}</span>
      </div>
    </div>
  );
}
