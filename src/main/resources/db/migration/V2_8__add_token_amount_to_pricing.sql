-- Add token amount fields to pricing_submissions
-- Token amount is a one-time non-refundable fee, does NOT affect estimatedMonthlyValue
ALTER TABLE pricing_submissions ADD COLUMN token_amount NUMERIC(14, 2);
ALTER TABLE pricing_submissions ADD COLUMN manager_token_amount NUMERIC(14, 2);
