-- Run this SQL Server rollout migration before deploying owner-scoped contact access.
-- Supply LegacyContactOwnerIdentifierSqlEscaped with sqlcmd's -v option after
-- escaping each single quote as two single quotes. See README.md for the safe
-- invocation. The transaction fails without changing data if that identifier
-- does not name exactly one user.

SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @legacy_owner_identifier nvarchar(254) = N'$(LegacyContactOwnerIdentifierSqlEscaped)';
    DECLARE @legacy_owner_id bigint;

    IF (SELECT COUNT(*) FROM users WHERE identifier = @legacy_owner_identifier) <> 1
        THROW 50001, 'LegacyContactOwnerIdentifierSqlEscaped must identify exactly one existing user.', 1;

    SELECT @legacy_owner_id = id
    FROM users
    WHERE identifier = @legacy_owner_identifier;

    IF COL_LENGTH(N'contacts', N'owner_id') IS NULL
        ALTER TABLE contacts ADD owner_id bigint NULL;

    -- Compile the backfill only after owner_id exists.
    EXEC sp_executesql
        N'UPDATE contacts SET owner_id = @owner_id WHERE owner_id IS NULL;

          IF EXISTS (SELECT 1 FROM contacts WHERE owner_id IS NULL)
              THROW 50002, ''Contact owner backfill did not assign every legacy contact.'', 1;',
        N'@owner_id bigint',
        @owner_id = @legacy_owner_id;

    ALTER TABLE contacts ALTER COLUMN owner_id bigint NOT NULL;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
