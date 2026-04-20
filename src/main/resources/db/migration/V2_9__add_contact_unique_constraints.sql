-- Unique contact per operator by email (where email is not null)
CREATE UNIQUE INDEX IF NOT EXISTS uq_contact_operator_email ON contacts(operator_id, email) WHERE email IS NOT NULL;

-- Unique contact per operator by mobile (where mobile is not null)
CREATE UNIQUE INDEX IF NOT EXISTS uq_contact_operator_mobile ON contacts(operator_id, mobile) WHERE mobile IS NOT NULL;
