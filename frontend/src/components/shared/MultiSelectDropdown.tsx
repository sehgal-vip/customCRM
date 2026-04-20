import { useState, useRef, useEffect } from "react";
import { ChevronDown, X } from "lucide-react";
import { cn } from "../../lib/utils";

interface MultiSelectDropdownProps {
  label: string;
  options: { value: string; label: string }[];
  selected: string[];
  onChange: (selected: string[]) => void;
  className?: string;
}

export default function MultiSelectDropdown({
  label,
  options,
  selected,
  onChange,
  className,
}: MultiSelectDropdownProps) {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  // Close on click outside
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen]);

  const toggle = (value: string) => {
    if (selected.includes(value)) {
      onChange(selected.filter((v) => v !== value));
    } else {
      onChange([...selected, value]);
    }
  };

  const clearAll = () => onChange([]);

  const displayText = () => {
    if (selected.length === 0) return `${label}: All`;
    if (selected.length === 1) {
      const opt = options.find((o) => o.value === selected[0]);
      return `${label}: ${opt?.label ?? selected[0]}`;
    }
    return `${label}: ${selected.length} selected`;
  };

  return (
    <div ref={containerRef} className={cn("relative", className)}>
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className={cn(
          "inline-flex items-center gap-1 rounded-lg border px-2.5 py-1.5 text-xs font-medium bg-white transition-colors",
          "focus:outline-none focus:ring-2 focus:ring-primary/40",
          selected.length > 0
            ? "border-primary/50 text-primary"
            : "border-border text-text-secondary"
        )}
      >
        <span>{displayText()}</span>
        <ChevronDown className={cn("h-3 w-3 transition-transform", isOpen && "rotate-180")} />
      </button>

      {isOpen && (
        <div className="absolute top-full left-0 mt-1 z-50 bg-white rounded-lg border border-border shadow-lg min-w-[180px] py-1 animate-[dropdown-enter_0.15s_ease-out]">
          {selected.length > 0 && (
            <button
              type="button"
              onClick={clearAll}
              className="w-full flex items-center gap-1.5 px-2.5 py-1 text-xs text-danger hover:bg-red-50 transition-colors"
            >
              <X className="h-3 w-3" />
              Clear all
            </button>
          )}

          {options.map((opt) => (
            <label
              key={opt.value}
              className="flex items-center gap-2 px-2.5 py-1 text-xs text-text-primary hover:bg-slate-50 cursor-pointer transition-colors"
            >
              <input
                type="checkbox"
                checked={selected.includes(opt.value)}
                onChange={() => toggle(opt.value)}
                className="rounded border-border accent-primary h-3.5 w-3.5"
              />
              <span>{opt.label}</span>
            </label>
          ))}
        </div>
      )}
    </div>
  );
}
