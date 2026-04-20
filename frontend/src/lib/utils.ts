import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";
import { PHASE_STAGES, STALE_THRESHOLDS } from "./constants";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function getPhaseForStage(stage: string): string {
  for (const [phase, stages] of Object.entries(PHASE_STAGES)) {
    if ((stages as readonly string[]).includes(stage)) return phase;
  }
  return "EARLY";
}

export function isOverdue(nextActionEta?: string | null): boolean {
  if (!nextActionEta) return false;
  return new Date(nextActionEta) < new Date();
}

export function isStale(stage: string, daysInStage: number): boolean {
  const phase = getPhaseForStage(stage);
  const threshold = STALE_THRESHOLDS[phase] ?? 14;
  return daysInStage > threshold;
}

/** Extract error message from Axios error or generic Error */
export function getErrorMessage(err: unknown, fallback = "Something went wrong"): string {
  if (err && typeof err === "object" && "response" in err) {
    const resp = (err as { response?: { data?: { message?: string } } }).response;
    if (resp?.data?.message) return resp.data.message;
  }
  if (err instanceof Error) return err.message;
  return fallback;
}
