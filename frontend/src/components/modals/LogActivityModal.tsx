import { useState, useEffect, useRef, useMemo } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { X, Loader2, MapPin, Video } from "lucide-react";
import { toast } from "sonner";
import { reportsApi, adminApi, attachmentsApi } from "../../services/apiEndpoints";
import {
  PHASE_STAGES,
  TEMPLATE_LABELS,
  DURATION_OPTIONS,
  DURATION_VALUES,
  NEXT_ACTION_OWNERS,
  NEXT_ACTION_OWNER_VALUES,
  CONTACT_ROLES,
  CONTACT_ROLE_VALUES,
  DEFAULT_OBJECTIONS,
  DEFAULT_BUYING_SIGNALS,
} from "../../lib/constants";
import { cn, getErrorMessage } from "../../lib/utils";
import PhaseSpecificFields from "../forms/PhaseSpecificFields";
import type { Deal, Contact } from "../../types";

function getPhase(stage: string): string {
  for (const [phase, stages] of Object.entries(PHASE_STAGES)) {
    if ((stages as readonly string[]).includes(stage)) return phase;
  }
  return "EARLY";
}

function selectTemplate(
  stage: string,
  activityType: string,
  isFirstInPhase: boolean
): string {
  const phase = getPhase(stage);
  const matrix: Record<string, Record<string, [string, string]>> = {
    EARLY: { FIELD_VISIT: ["T1", "T2"], VIRTUAL: ["T3", "T4"] },
    COMMERCIAL: { FIELD_VISIT: ["T5", "T6"], VIRTUAL: ["T7", "T8"] },
    CLOSURE: { FIELD_VISIT: ["T9", "T10"], VIRTUAL: ["T11", "T12"] },
  };
  const [first, followup] = matrix[phase][activityType];
  return isFirstInPhase ? first : followup;
}

interface LogActivityModalProps {
  open: boolean;
  onClose: () => void;
  deal: Deal;
  contacts: Contact[];
  isFirstInPhase: boolean;
}

export default function LogActivityModal({
  open,
  onClose,
  deal,
  contacts,
  isFirstInPhase,
}: LogActivityModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();

  const {
    register,
    handleSubmit,
    watch,
    reset,
    setValue,
    formState: { isValid },
  } = useForm<Record<string, unknown>>({
    mode: "onChange",
    defaultValues: {
      activityType: "FIELD_VISIT",
      interactionDatetime: new Date().toISOString().slice(0, 16),
      duration: "",
      contactId: "",
      contactRole: "",
      newContactName: "",
      newContactMobile: "",
      newContactEmail: "",
      phaseSpecificData: {},
      objections: [],
      buyingSignals: [],
      notes: "",
      nextAction: "",
      nextActionEta: "",
      nextActionOwner: "Self",
      noFurtherAction: false,
    },
  });

  const activityType = (watch("activityType") as string) || "FIELD_VISIT";
  const contactId = watch("contactId") as string;
  const isNewContact = contactId === "NEW";
  const objections = (watch("objections") as string[]) ?? [];
  const buyingSignals = (watch("buyingSignals") as string[]) ?? [];
  const noFurtherAction = watch("noFurtherAction") as boolean;

  const stageNum = parseInt(deal.currentStage.replace("STAGE_", ""), 10);
  const showNoFurtherAction = stageNum >= 8;

  const templateType = useMemo(
    () => selectTemplate(deal.currentStage, activityType, isFirstInPhase),
    [deal.currentStage, activityType, isFirstInPhase]
  );

  // Fetch objections and buying signals from taxonomy API (not hardcoded)
  const { data: objectionItems } = useQuery({
    queryKey: ["taxonomy", "OBJECTION"],
    queryFn: () => adminApi.listTaxonomy("OBJECTION"),
    enabled: open,
    staleTime: 300_000,
  });
  const { data: signalItems } = useQuery({
    queryKey: ["taxonomy", "BUYING_SIGNAL"],
    queryFn: () => adminApi.listTaxonomy("BUYING_SIGNAL"),
    enabled: open,
    staleTime: 300_000,
  });
  const objectionOptions = (objectionItems || [])
    .filter((i) => i.active)
    .map((i) => i.value);
  const signalOptions = (signalItems || [])
    .filter((i) => i.active)
    .map((i) => i.value);

  // Attachment state
  interface FileAttachment {
    file: File;
    categoryTag: string;
    status: "uploading" | "done" | "error";
    progress: number;
    fileKey?: string;
  }
  const [files, setFiles] = useState<FileAttachment[]>([]);
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Attachment tag options
  const { data: tagItems } = useQuery({
    queryKey: ["taxonomy", "ATTACHMENT_TAG"],
    queryFn: () => adminApi.listTaxonomy("ATTACHMENT_TAG"),
    enabled: open,
    staleTime: 300_000,
  });
  const tagOptions = (tagItems || []).filter((t) => t.active).map((t) => t.value);
  const DEFAULT_TAGS = ["Site Photos", "Route Maps", "Competitor Materials", "Fleet Documents", "Depot Photos", "Charging Infra", "Meeting Notes", "Proposal/Quote", "Contract Draft", "Other"];
  const availableTags = tagOptions.length > 0 ? tagOptions : DEFAULT_TAGS;

  const MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

  const addFiles = (newFiles: FileList | File[]) => {
    const validated = Array.from(newFiles).filter(f => {
      if (f.size > MAX_FILE_SIZE) {
        toast.error(`${f.name} exceeds 50MB limit`);
        return false;
      }
      return true;
    });

    validated.forEach(file => {
      setFiles(prev => {
        const newIndex = prev.length;
        // Start upload for this file
        startUpload(file, newIndex);
        return [...prev, { file, categoryTag: "", status: "uploading" as const, progress: 10 }];
      });
    });
  };

  const startUpload = async (file: File, index: number) => {
    // Simulate initial progress
    setFiles(prev => prev.map((f, i) => i === index ? { ...f, progress: 30 } : f));

    try {
      const result = await attachmentsApi.upload(file, `deals/${deal.id}/reports`);
      setFiles(prev => prev.map((f, i) => i === index
        ? { ...f, status: "done" as const, progress: 100, fileKey: result.fileKey }
        : f
      ));
    } catch {
      setFiles(prev => prev.map((f, i) => i === index
        ? { ...f, status: "error" as const, progress: 0 }
        : f
      ));
    }
  };

  const retryUpload = (index: number) => {
    const f = files[index];
    if (!f) return;
    setFiles(prev => prev.map((item, i) => i === index
      ? { ...item, status: "uploading" as const, progress: 10 }
      : item
    ));
    startUpload(f.file, index);
  };

  const removeFile = (index: number) => {
    setFiles(prev => prev.filter((_, i) => i !== index));
  };

  const updateTag = (index: number, tag: string) => {
    setFiles(prev => prev.map((f, i) => i === index ? { ...f, categoryTag: tag } : f));
  };

  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) =>
      reportsApi.submit(deal.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deal", deal.id] });
      queryClient.invalidateQueries({ queryKey: ["reports", deal.id] });
      toast.success("Activity logged");
      setFiles([]);
      reset();
      onClose();
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to log activity"));
    },
  });

  // Render-time reset when modal closes
  const [wasOpen, setWasOpen] = useState(false);
  if (open && !wasOpen) {
    setWasOpen(true);
  }
  if (!open && wasOpen) {
    setWasOpen(false);
    reset({
      activityType: "FIELD_VISIT",
      interactionDatetime: new Date().toISOString().slice(0, 16),
      duration: "",
      contactId: "",
      contactRole: "",
      newContactName: "",
      newContactMobile: "",
      newContactEmail: "",
      phaseSpecificData: {},
      objections: [],
      buyingSignals: [],
      notes: "",
      nextAction: "",
      nextActionEta: "",
      nextActionOwner: "Self",
      noFurtherAction: false,
    });
  }

  useEffect(() => {
    const dlg = dialogRef.current;
    if (!dlg) return;
    if (open && !dlg.open) dlg.showModal();
    else if (!open && dlg.open) dlg.close();
  }, [open]);

  const toggleChip = (
    field: "objections" | "buyingSignals",
    value: string,
    current: string[]
  ) => {
    const next = current.includes(value)
      ? current.filter((v) => v !== value)
      : [...current, value];
    setValue(field, next, { shouldDirty: true });
  };

  const onSubmit = async (data: Record<string, unknown>) => {
    const payload: Record<string, unknown> = {
      activityType: data.activityType,
      interactionDatetime: data.interactionDatetime
        ? new Date(data.interactionDatetime as string).toISOString()
        : undefined,
      contactRole:
        CONTACT_ROLE_VALUES[data.contactRole as string] ||
        (data.contactRole as string).toUpperCase().replace(/ /g, "_"),
      duration:
        DURATION_VALUES[data.duration as string] || data.duration,
      phaseSpecificData: data.phaseSpecificData || {},
      buyingSignals: data.buyingSignals || [],
      objections: data.objections || [],
      notes: data.notes || "",
      nextAction: data.noFurtherAction
        ? "No further action"
        : data.nextAction,
      nextActionEta: data.noFurtherAction ? null : data.nextActionEta,
      nextActionOwner: data.noFurtherAction
        ? "SELF"
        : NEXT_ACTION_OWNER_VALUES[data.nextActionOwner as string] ||
          (data.nextActionOwner as string).toUpperCase().replace(/ /g, "_"),
    };

    // Handle contact: existing or new
    const cId = data.contactId as string;
    if (cId && cId !== "NEW") {
      payload.contactId = parseInt(cId, 10);
    } else if (cId === "NEW") {
      payload.newContact = {
        name: data.newContactName,
        mobile: data.newContactMobile || null,
        email: data.newContactEmail || null,
        role:
          CONTACT_ROLE_VALUES[data.contactRole as string] ||
          (data.contactRole as string).toUpperCase().replace(/ /g, "_"),
      };
    }

    // Check all files are uploaded and tagged
    const hasUnfinished = files.some(f => f.status === "uploading");
    if (hasUnfinished) {
      toast.error("Please wait for all files to finish uploading");
      return;
    }
    const hasUntagged = files.filter(f => f.status === "done").some(f => !f.categoryTag);
    if (hasUntagged) {
      toast.error("All attachments must have a category tag");
      return;
    }
    const hasErrors = files.some(f => f.status === "error");
    if (hasErrors) {
      toast.error("Some files failed to upload. Please retry or remove them.");
      return;
    }

    payload.attachments = files
      .filter(f => f.status === "done" && f.fileKey)
      .map(f => ({
        fileName: f.file.name,
        fileKey: f.fileKey!,
        fileSize: f.file.size,
        categoryTag: f.categoryTag,
      }));

    mutation.mutate(payload);
  };

  if (!open) return null;

  return (
    <dialog
      ref={dialogRef}
      onCancel={onClose}
      className="backdrop:bg-black/50 backdrop:backdrop-blur-sm bg-transparent p-0 m-auto rounded-2xl max-w-xl w-full"
    >
      <div className="bg-white rounded-2xl shadow-[0_25px_50px_rgba(0,0,0,0.15)] max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border shrink-0">
          <div>
            <h2 className="text-lg font-semibold text-text-primary">
              Log Activity
            </h2>
            <p className="text-xs text-text-muted mt-0.5">{deal.name}</p>
          </div>
          <button
            type="button"
            aria-label="Close"
            onClick={onClose}
            className="p-1 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Body */}
        <form
          onSubmit={handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-6 py-5 space-y-5"
        >
          {/* Activity Type Toggle */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1.5">
              Activity Type
            </label>
            <div className="inline-flex rounded-lg bg-slate-100 p-0.5">
              <button
                type="button"
                onClick={() => setValue("activityType", "FIELD_VISIT")}
                className={cn(
                  "inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md transition-all",
                  activityType === "FIELD_VISIT"
                    ? "bg-white shadow-sm text-teal-700"
                    : "text-text-muted hover:text-text-secondary"
                )}
              >
                <MapPin className="h-3.5 w-3.5" />
                Field Visit
              </button>
              <button
                type="button"
                onClick={() => setValue("activityType", "VIRTUAL")}
                className={cn(
                  "inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md transition-all",
                  activityType === "VIRTUAL"
                    ? "bg-white shadow-sm text-blue-700"
                    : "text-text-muted hover:text-text-secondary"
                )}
              >
                <Video className="h-3.5 w-3.5" />
                Virtual
              </button>
            </div>
          </div>

          {/* Contact */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Contact Person <span className="text-danger">*</span>
            </label>
            <select
              {...register("contactId", { required: true })}
              autoFocus
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
            >
              <option value="">Select contact...</option>
              {contacts.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} ({c.role}){c.incomplete ? " - Incomplete" : ""}
                </option>
              ))}
              <option value="NEW">+ New Contact</option>
            </select>
          </div>

          {/* New Contact Inline */}
          {isNewContact && (
            <div className="space-y-3 rounded-lg border border-border p-3 bg-slate-50">
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  Name <span className="text-danger">*</span>
                </label>
                <input
                  {...register("newContactName")}
                  className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                    Mobile
                  </label>
                  <input
                    {...register("newContactMobile")}
                    type="tel"
                    className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                    Email
                  </label>
                  <input
                    {...register("newContactEmail")}
                    type="email"
                    className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                  />
                </div>
              </div>
              <p className="text-[10px] text-text-muted">
                At least one of mobile or email is required.
              </p>
            </div>
          )}

          {/* Contact Role */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Contact Role <span className="text-danger">*</span>
            </label>
            <select
              {...register("contactRole", { required: true })}
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
            >
              <option value="">Select role...</option>
              {CONTACT_ROLES.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>

          {/* Date & Time + Duration row */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Interaction Date & Time <span className="text-danger">*</span>
              </label>
              <input
                {...register("interactionDatetime", { required: true })}
                type="datetime-local"
                className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
              />
            </div>
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Duration <span className="text-danger">*</span>
              </label>
              <select
                {...register("duration", { required: true })}
                className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
              >
                <option value="">Select...</option>
                {DURATION_OPTIONS.map((d) => (
                  <option key={d} value={d}>
                    {d}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Template Indicator */}
          <div className="text-xs text-text-muted">
            Template: <span className="font-medium">{templateType} -- {TEMPLATE_LABELS[templateType]}</span>
          </div>

          {/* Phase Specific Fields */}
          <PhaseSpecificFields
            templateType={templateType}
            register={register as unknown as import("react-hook-form").UseFormRegister<Record<string, unknown>>}
            setValue={setValue as unknown as import("react-hook-form").UseFormSetValue<Record<string, unknown>>}
            watch={watch as unknown as import("react-hook-form").UseFormWatch<Record<string, unknown>>}
          />

          {/* Objections */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1.5">
              Objections Raised
            </label>
            <div className="flex flex-wrap gap-1.5">
              {(objectionOptions.length > 0 ? objectionOptions : DEFAULT_OBJECTIONS as unknown as string[]).map((obj: string) => (
                <button
                  key={obj}
                  type="button"
                  onClick={() => toggleChip("objections", obj, objections)}
                  className={cn(
                    "px-2.5 py-1 rounded-full text-xs font-medium border transition-colors",
                    objections.includes(obj)
                      ? "bg-red-50 border-red-300 text-red-700"
                      : "bg-white border-border text-text-secondary hover:bg-slate-50"
                  )}
                >
                  {obj}
                </button>
              ))}
            </div>
          </div>

          {/* Buying Signals */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1.5">
              Buying Signals
            </label>
            <div className="flex flex-wrap gap-1.5">
              {(signalOptions.length > 0 ? signalOptions : DEFAULT_BUYING_SIGNALS as unknown as string[]).map((sig: string) => (
                <button
                  key={sig}
                  type="button"
                  onClick={() => toggleChip("buyingSignals", sig, buyingSignals)}
                  className={cn(
                    "px-2.5 py-1 rounded-full text-xs font-medium border transition-colors",
                    buyingSignals.includes(sig)
                      ? "bg-teal-50 border-teal-300 text-teal-700"
                      : "bg-white border-border text-text-secondary hover:bg-slate-50"
                  )}
                >
                  {sig}
                </button>
              ))}
            </div>
          </div>

          {/* Notes */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Notes
            </label>
            <textarea
              {...register("notes")}
              rows={3}
              placeholder="Additional context..."
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary resize-none"
            />
          </div>

          {/* Attachments */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Attachments
            </label>

            {/* Drop zone */}
            <div
              className={cn(
                "rounded-lg border-2 border-dashed p-4 text-center cursor-pointer transition-colors",
                dragOver ? "border-primary bg-primary-light/30" : "border-border hover:border-primary/50"
              )}
              onClick={() => fileInputRef.current?.click()}
              onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
              onDragLeave={() => setDragOver(false)}
              onDrop={(e) => {
                e.preventDefault();
                setDragOver(false);
                if (e.dataTransfer.files.length) addFiles(e.dataTransfer.files);
              }}
            >
              <input
                ref={fileInputRef}
                type="file"
                multiple
                className="hidden"
                onChange={(e) => { if (e.target.files) addFiles(e.target.files); e.target.value = ""; }}
              />
              <p className="text-sm text-text-muted">
                {dragOver ? "Drop files here" : "Click to browse or drag & drop files"}
              </p>
              <p className="text-[10px] text-text-muted mt-1">50MB per file limit</p>
            </div>

            {/* File list */}
            {files.length > 0 && (
              <div className="mt-2 space-y-1.5">
                {files.map((f, i) => (
                  <div key={i} className="flex items-center gap-2 rounded-lg border border-border px-3 py-2 bg-white">
                    <div className="flex-1 min-w-0">
                      <p className="text-xs font-medium text-text-primary truncate">{f.file.name}</p>
                      <div className="flex items-center gap-2">
                        <p className="text-[10px] text-text-muted">{(f.file.size / 1024).toFixed(0)} KB</p>
                        {f.status === "uploading" && (
                          <div className="flex items-center gap-1.5 flex-1">
                            <div className="flex-1 h-1.5 bg-slate-200 rounded-full overflow-hidden">
                              <div className="h-full bg-primary rounded-full transition-all duration-300" style={{ width: `${f.progress}%` }} />
                            </div>
                            <span className="text-[10px] text-primary font-medium">{f.progress}%</span>
                          </div>
                        )}
                        {f.status === "done" && (
                          <span className="text-[10px] text-emerald-600 font-medium flex items-center gap-0.5">
                            ✓ Uploaded
                          </span>
                        )}
                        {f.status === "error" && (
                          <span className="flex items-center gap-1">
                            <span className="text-[10px] text-danger font-medium">Failed</span>
                            <button type="button" onClick={() => retryUpload(i)} className="text-[10px] text-primary underline">Retry</button>
                          </span>
                        )}
                      </div>
                    </div>
                    {f.status === "done" && (
                      <select
                        value={f.categoryTag}
                        onChange={(e) => updateTag(i, e.target.value)}
                        className="rounded border border-border px-1.5 py-0.5 text-[10px] text-text-secondary bg-white min-w-[100px]"
                      >
                        <option value="">Tag...</option>
                        {availableTags.map((tag: string) => (
                          <option key={tag} value={tag}>{tag}</option>
                        ))}
                      </select>
                    )}
                    <button
                      type="button"
                      onClick={() => removeFile(i)}
                      className="p-0.5 rounded hover:bg-slate-100 text-text-muted"
                    >
                      <X className="h-3.5 w-3.5" />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Next Action Section */}
          <div className="rounded-lg bg-slate-50 border border-border p-4 space-y-3">
            <h4 className="text-xs font-semibold uppercase tracking-wider text-text-muted">
              Next Action
            </h4>

            {showNoFurtherAction && (
              <label className="flex items-center gap-2 text-sm text-text-secondary">
                <input
                  type="checkbox"
                  {...register("noFurtherAction")}
                  className="rounded border-border text-primary focus:ring-primary/40"
                />
                No further action
              </label>
            )}

            {!noFurtherAction && (
              <>
                <div>
                  <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                    Next Step <span className="text-danger">*</span>
                  </label>
                  <input
                    {...register("nextAction", { required: !noFurtherAction })}
                    type="text"
                    placeholder="Specific next step..."
                    className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                  />
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                      Due Date <span className="text-danger">*</span>
                    </label>
                    <input
                      {...register("nextActionEta", { required: !noFurtherAction })}
                      type="date"
                      className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                      Owner <span className="text-danger">*</span>
                    </label>
                    <select
                      {...register("nextActionOwner", { required: !noFurtherAction })}
                      className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                    >
                      {NEXT_ACTION_OWNERS.map((o) => (
                        <option key={o} value={o}>
                          {o}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              </>
            )}
          </div>

          {/* Error */}
          {mutation.isError && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
              Failed to submit report. Please try again.
            </div>
          )}
        </form>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-border shrink-0">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm rounded-lg text-text-secondary hover:bg-slate-100 transition-colors"
          >
            Cancel
          </button>
          <button
            type="button"
            disabled={!isValid || mutation.isPending || files.some(f => f.status === "uploading")}
            onClick={handleSubmit(onSubmit)}
            className={cn(
              "px-4 py-2 text-sm font-medium rounded-lg text-white shadow-sm transition-colors",
              "bg-primary hover:bg-primary-hover",
              "disabled:opacity-50 disabled:cursor-not-allowed"
            )}
          >
            {mutation.isPending ? (
              <span className="inline-flex items-center gap-2">
                <Loader2 className="h-4 w-4 animate-spin" />
                Submitting...
              </span>
            ) : (
              "Submit Report"
            )}
          </button>
        </div>
      </div>
    </dialog>
  );
}
