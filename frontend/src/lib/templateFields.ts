export interface FieldDef {
  name: string;
  label: string;
  type: "text" | "number" | "select" | "multiselect" | "yesno" | "textarea";
  options?: string[];
  required?: boolean;
  placeholder?: string;
}

export const TEMPLATE_FIELDS: Record<string, FieldDef[]> = {
  T1: [
    { name: "operatorType", label: "Operator Type", type: "select", options: ["Private fleet", "Govt contract", "School/corporate", "Mixed"], required: true },
    { name: "currentFleetSize", label: "Current Fleet Size", type: "number", required: true },
    { name: "currentVehicleType", label: "Current Vehicle Type", type: "multiselect", options: ["Diesel bus", "CNG bus", "EV (already)", "Tempo", "Other"], required: true },
    { name: "numberOfRoutes", label: "Number of Routes", type: "number" },
    { name: "primaryUseCase", label: "Primary Use Case", type: "select", options: ["City transport", "Intercity", "School shuttle", "Corporate shuttle", "Last-mile", "Mixed"], required: true },
    { name: "interestLevel", label: "Interest Level", type: "select", options: ["Hot", "Warm", "Cold"], required: true },
    { name: "decisionMakerMet", label: "Decision Maker Met?", type: "yesno", required: true },
    { name: "decisionMakerName", label: "Decision Maker Name", type: "text", placeholder: "Name and designation" },
  ],
  T2: [
    { name: "interestLevelUpdate", label: "Interest Level Update", type: "select", options: ["Hot", "Warm", "Cold", "Dropped"], required: true },
    { name: "interestChangeReason", label: "Interest Change Reason", type: "text" },
    { name: "decisionMakerAccess", label: "Decision Maker Access", type: "select", options: ["Met", "Scheduled", "Gatekeeper blocking", "Not yet attempted"], required: true },
    { name: "newInfoGathered", label: "New Info Gathered", type: "text" },
  ],
  T3: [
    { name: "operatorType", label: "Operator Type", type: "select", options: ["Private fleet", "Govt contract", "School/corporate", "Mixed"], required: true },
    { name: "currentFleetSize", label: "Current Fleet Size", type: "number" },
    { name: "primaryUseCase", label: "Primary Use Case", type: "select", options: ["City transport", "Intercity", "School shuttle", "Corporate shuttle", "Last-mile", "Mixed"] },
    { name: "interestLevel", label: "Interest Level", type: "select", options: ["Hot", "Warm", "Cold"], required: true },
    { name: "followUpVisitNeeded", label: "Follow-up Visit Needed?", type: "yesno", required: true },
    { name: "informationShared", label: "Information Shared", type: "multiselect", options: ["Brochure", "Pricing overview", "Case study", "None yet"] },
  ],
  T4: [
    { name: "interestLevelUpdate", label: "Interest Level Update", type: "select", options: ["Hot", "Warm", "Cold", "Dropped"], required: true },
    { name: "decisionMakerIdentified", label: "Decision Maker Identified?", type: "yesno", required: true },
    { name: "informationRequested", label: "Information Requested", type: "text" },
    { name: "informationShared", label: "Information Shared", type: "multiselect", options: ["Brochure", "Pricing overview", "Case study", "Route analysis", "None"] },
  ],
  T5: [
    { name: "routesAssessed", label: "Routes Assessed", type: "text", required: true, placeholder: "Key routes with approximate km" },
    { name: "dailyKmPerVehicle", label: "Daily km per Vehicle", type: "number", required: true },
    { name: "depotChargingFeasibility", label: "Depot Charging Feasibility", type: "select", options: ["Feasible", "Needs infra work", "Not feasible", "Needs assessment"], required: true },
    { name: "depotSizeCapacity", label: "Depot Size / Capacity", type: "select", options: ["< 10 vehicles", "10-25", "25-50", "50+"] },
    { name: "electricityAvailability", label: "Electricity Availability", type: "select", options: ["Reliable", "Intermittent", "Needs DG backup", "Unknown"] },
    { name: "currentMonthlyFuelCost", label: "Current Monthly Fuel Cost (INR)", type: "number" },
    { name: "peakFleetRequirement", label: "Peak Fleet Requirement", type: "number", required: true },
    { name: "keyPainPoints", label: "Key Pain Points", type: "multiselect", options: ["High fuel costs", "Frequent breakdowns", "Driver shortage", "Regulatory pressure", "Pollution penalties", "Rising maintenance", "Other"], required: true },
    { name: "proposalDiscussed", label: "Proposal Discussed?", type: "yesno" },
    { name: "competitorMentioned", label: "Competitor Mentioned?", type: "yesno" },
    { name: "competitorDetails", label: "Competitor Details", type: "text" },
  ],
  T6: [
    { name: "updatedFleetRequirement", label: "Updated Fleet Requirement", type: "number" },
    { name: "routeViabilityConfirmed", label: "Route Viability Confirmed?", type: "select", options: ["Confirmed", "Partially", "Needs re-assessment"], required: true },
    { name: "chargingInfraUpdate", label: "Charging Infra Update", type: "text" },
    { name: "negotiationStatus", label: "Negotiation Status", type: "select", options: ["Pre-proposal", "Proposal shared", "Awaiting approval", "Near closure"], required: true },
    { name: "discountRequested", label: "Discount Requested?", type: "yesno" },
    { name: "operatorDecisionTimeline", label: "Operator Decision Timeline", type: "select", options: ["This week", "2 weeks", "1 month", "No clarity", "Delayed"], required: true },
    { name: "stakeholdersInvolved", label: "Stakeholders Involved", type: "multiselect", options: ["Owner", "Finance head", "Fleet manager", "Board/committee", "External advisor"] },
  ],
  T7: [
    { name: "routeDataReceived", label: "Route Data Received?", type: "yesno", required: true },
    { name: "documentsSharedByOperator", label: "Documents Shared by Operator", type: "multiselect", options: ["Route maps", "Fleet register", "Fuel bills", "None yet"] },
    { name: "keyRequirementsDiscussed", label: "Key Requirements Discussed", type: "text", required: true },
    { name: "siteVisitScheduled", label: "Site Visit Scheduled?", type: "select", options: ["Yes", "No", "Date pending"], required: true },
    { name: "proposalShared", label: "Proposal Shared?", type: "select", options: ["Sent via email", "Discussed on call", "Not yet"] },
    { name: "operatorReaction", label: "Operator Reaction", type: "select", options: ["Positive", "Needs time", "Price concerns", "Comparing options", "Negative"] },
  ],
  T8: [
    { name: "negotiationProgress", label: "Negotiation Progress", type: "select", options: ["Pre-proposal", "Proposal shared", "Near closure"] },
    { name: "revisedTermsCommunicated", label: "Revised Terms Communicated?", type: "yesno" },
    { name: "operatorDecisionTimeline", label: "Operator Decision Timeline", type: "select", options: ["This week", "2 weeks", "1 month", "No clarity", "Delayed"], required: true },
    { name: "pricingApprovalStatus", label: "Pricing Approval Status", type: "select", options: ["Not needed yet", "Pending submission", "Submitted"], required: true },
    { name: "keyBlocker", label: "Key Blocker", type: "text" },
    { name: "dataGapsRemaining", label: "Data Gaps Remaining", type: "text" },
  ],
  T9: [
    { name: "commitmentStatus", label: "Commitment Status", type: "select", options: ["Verbal yes", "Conditional yes (subject to...)", "Still deciding"], required: true },
    { name: "conditions", label: "Conditions (if any)", type: "text" },
    { name: "documentsCollected", label: "Documents Collected", type: "multiselect", options: ["Lease agreement", "KYC docs", "Fleet register", "Route permits", "Insurance", "Bank details", "Other"] },
    { name: "documentsPending", label: "Documents Pending", type: "multiselect", options: ["Lease agreement", "KYC docs", "Fleet register", "Route permits", "Insurance", "Bank details", "Other"] },
    { name: "preferredDeliveryTimeline", label: "Preferred Delivery Timeline", type: "select", options: ["Immediate", "2 weeks", "1 month", "2 months", "3+ months"], required: true },
    { name: "vehicleSpecsConfirmed", label: "Vehicle Specs Confirmed?", type: "yesno", required: true },
    { name: "signingTimeline", label: "Signing Timeline", type: "select", options: ["This week", "2 weeks", "1 month", "Unclear"], required: true },
  ],
  T10: [
    { name: "documentsCollectedThisVisit", label: "Documents Collected (This Visit)", type: "multiselect", options: ["Lease agreement", "KYC docs", "Fleet register", "Route permits", "Insurance", "Bank details", "Other"], required: true },
    { name: "documentsStillPending", label: "Documents Still Pending", type: "multiselect", options: ["Lease agreement", "KYC docs", "Fleet register", "Route permits", "Insurance", "Bank details", "Other"], required: true },
    { name: "signingStatus", label: "Signing Status", type: "select", options: ["Signed", "Scheduled", "Delayed", "Blocked"], required: true },
    { name: "deliveryTimelineUpdate", label: "Delivery Timeline Update", type: "select", options: ["On track", "Delayed", "Rescheduled"], required: true },
    { name: "blockerEscalation", label: "Blocker / Escalation Needed?", type: "select", options: ["No blocker", "Operator-side delay", "Internal (ops/finance)", "Vehicle availability"] },
  ],
  T11: [
    { name: "commitmentReconfirmed", label: "Commitment Reconfirmed?", type: "yesno", required: true },
    { name: "documentsSharedViaEmail", label: "Documents Shared via Email?", type: "yesno" },
    { name: "documentsPending", label: "Documents Pending", type: "multiselect", options: ["Lease agreement", "KYC docs", "Fleet register", "Route permits", "Insurance", "Bank details", "Other"], required: true },
    { name: "signingMode", label: "Signing Mode", type: "select", options: ["In-person", "Courier", "Digital (e-sign)", "Not discussed"] },
    { name: "deliveryCoordinationStarted", label: "Delivery Coordination Started?", type: "yesno", required: true },
  ],
  T12: [
    { name: "documentSubmissionStatus", label: "Document Submission Status", type: "select", options: ["All received", "Partially", "Pending operator", "Pending our review"], required: true },
    { name: "signingStatus", label: "Signing Status", type: "select", options: ["Signed", "Scheduled", "Delayed", "Blocked"], required: true },
    { name: "deliveryStatus", label: "Delivery Status", type: "select", options: ["Vehicle allocated", "In transit", "Delivered", "Delayed"] },
    { name: "postSigningIssues", label: "Post-signing Issues", type: "text" },
  ],
};
