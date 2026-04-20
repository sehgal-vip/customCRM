import type { UseFormRegister, UseFormSetValue, UseFormWatch } from "react-hook-form";
import { cn } from "../../lib/utils";
import { TEMPLATE_FIELDS, type FieldDef } from "../../lib/templateFields";

interface PhaseSpecificFieldsProps {
  templateType: string;
  register: UseFormRegister<Record<string, unknown>>;
  setValue: UseFormSetValue<Record<string, unknown>>;
  watch: UseFormWatch<Record<string, unknown>>;
}

export default function PhaseSpecificFields({
  templateType,
  register,
  setValue,
  watch,
}: PhaseSpecificFieldsProps) {
  const fields = TEMPLATE_FIELDS[templateType];
  if (!fields || fields.length === 0) {
    return null;
  }

  return (
    <div className="space-y-4">
      <h3 className="text-xs font-semibold uppercase tracking-wider text-text-muted">
        Phase-Specific Fields
      </h3>
      {fields.map((field) => (
        <FieldRenderer
          key={field.name}
          field={field}
          register={register}
          setValue={setValue}
          watch={watch}
        />
      ))}
    </div>
  );
}

function FieldRenderer({
  field,
  register,
  setValue,
  watch,
}: {
  field: FieldDef;
  register: UseFormRegister<Record<string, unknown>>;
  setValue: UseFormSetValue<Record<string, unknown>>;
  watch: UseFormWatch<Record<string, unknown>>;
}) {
  const fieldKey = `phaseSpecificData.${field.name}`;

  if (field.type === "text") {
    return (
      <div>
        <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
          {field.label} {field.required && <span className="text-danger">*</span>}
        </label>
        <input
          {...register(fieldKey)}
          type="text"
          placeholder={field.placeholder}
          className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
        />
      </div>
    );
  }

  if (field.type === "number") {
    return (
      <div>
        <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
          {field.label} {field.required && <span className="text-danger">*</span>}
        </label>
        <input
          {...register(fieldKey)}
          type="number"
          placeholder={field.placeholder}
          className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
        />
      </div>
    );
  }

  if (field.type === "textarea") {
    return (
      <div>
        <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
          {field.label} {field.required && <span className="text-danger">*</span>}
        </label>
        <textarea
          {...register(fieldKey)}
          rows={3}
          className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary resize-none"
        />
      </div>
    );
  }

  if (field.type === "select") {
    return (
      <div>
        <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
          {field.label} {field.required && <span className="text-danger">*</span>}
        </label>
        <select
          {...register(fieldKey)}
          className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
        >
          <option value="">Select...</option>
          {field.options?.map((opt) => (
            <option key={opt} value={opt}>
              {opt}
            </option>
          ))}
        </select>
      </div>
    );
  }

  if (field.type === "yesno") {
    return (
      <div>
        <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
          {field.label} {field.required && <span className="text-danger">*</span>}
        </label>
        <select
          {...register(fieldKey)}
          className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
        >
          <option value="">Select...</option>
          <option value="Yes">Yes</option>
          <option value="No">No</option>
        </select>
      </div>
    );
  }

  if (field.type === "multiselect") {
    const currentValue = (watch(fieldKey) as string[] | undefined) ?? [];

    const toggleOption = (opt: string) => {
      const next = currentValue.includes(opt)
        ? currentValue.filter((v) => v !== opt)
        : [...currentValue, opt];
      setValue(fieldKey, next, { shouldDirty: true });
    };

    return (
      <div>
        <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
          {field.label} {field.required && <span className="text-danger">*</span>}
        </label>
        <div className="flex flex-wrap gap-1.5">
          {field.options?.map((opt) => {
            const selected = currentValue.includes(opt);
            return (
              <button
                key={opt}
                type="button"
                onClick={() => toggleOption(opt)}
                className={cn(
                  "px-2.5 py-1 rounded-full text-xs font-medium border transition-colors",
                  selected
                    ? "bg-teal-50 border-teal-300 text-teal-700"
                    : "bg-white border-border text-text-secondary hover:bg-slate-50"
                )}
              >
                {opt}
              </button>
            );
          })}
        </div>
      </div>
    );
  }

  return null;
}
