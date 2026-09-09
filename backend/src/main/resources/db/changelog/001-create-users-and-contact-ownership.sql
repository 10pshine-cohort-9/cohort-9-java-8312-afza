IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        first_name NVARCHAR(100) NOT NULL,
        last_name NVARCHAR(100) NOT NULL,
        identifier NVARCHAR(254) NOT NULL,
        identifier_type NVARCHAR(10) NOT NULL,
        password_hash NVARCHAR(60) NOT NULL,
        CONSTRAINT uk_users_identifier UNIQUE (identifier)
    );
END;

IF OBJECT_ID(N'dbo.contacts', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.contacts (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        first_name NVARCHAR(255) NULL,
        last_name NVARCHAR(255) NULL,
        title NVARCHAR(255) NULL,
        owner_id BIGINT NULL
    );
END;

IF COL_LENGTH(N'dbo.contacts', N'owner_id') IS NULL
BEGIN
    ALTER TABLE dbo.contacts ADD owner_id BIGINT NULL;
END;

IF EXISTS (SELECT 1 FROM dbo.contacts WHERE owner_id IS NULL)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE identifier = N'legacy-contacts@invalid.local')
    BEGIN
        INSERT INTO dbo.users (first_name, last_name, identifier, identifier_type, password_hash)
        VALUES (N'Legacy', N'Contacts', N'legacy-contacts@invalid.local', N'EMAIL',
                N'$2a$10$7EqJtq98hPqEX7fNZaFWoO5uM6Q9FhCwG7rj2KJxYvXvVQXfY3uSa');
    END;

    UPDATE dbo.contacts
    SET owner_id = (SELECT id FROM dbo.users WHERE identifier = N'legacy-contacts@invalid.local')
    WHERE owner_id IS NULL;
END;

ALTER TABLE dbo.contacts ALTER COLUMN owner_id BIGINT NOT NULL;

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'fk_contacts_owner')
BEGIN
    ALTER TABLE dbo.contacts
        ADD CONSTRAINT fk_contacts_owner FOREIGN KEY (owner_id) REFERENCES dbo.users(id);
END;

IF OBJECT_ID(N'dbo.email_addresses', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.email_addresses (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        email NVARCHAR(255) NULL,
        label NVARCHAR(255) NULL,
        contact_id BIGINT NULL,
        CONSTRAINT fk_email_addresses_contact FOREIGN KEY (contact_id) REFERENCES dbo.contacts(id)
    );
END;

IF OBJECT_ID(N'dbo.phone_numbers', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.phone_numbers (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        phone_number NVARCHAR(255) NULL,
        label NVARCHAR(255) NULL,
        contact_id BIGINT NULL,
        CONSTRAINT fk_phone_numbers_contact FOREIGN KEY (contact_id) REFERENCES dbo.contacts(id)
    );
END;
