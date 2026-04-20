import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import StageBadge from "./StageBadge";

describe("StageBadge", () => {
  it("renders stage name for valid stage", () => {
    render(<StageBadge stage="STAGE_1" />);
    expect(screen.getByText("Lead Captured")).toBeInTheDocument();
  });

  it("renders fallback for unknown stage", () => {
    render(<StageBadge stage="UNKNOWN" />);
    expect(screen.getByText("UNKNOWN")).toBeInTheDocument();
  });
});
