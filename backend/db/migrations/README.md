# Database rollout migrations

Run these SQL Server scripts in numeric order before deploying the corresponding application change.

For `002_backfill_contact_owners.sql`, choose the existing account that should own all contacts created before ownership was introduced. Escape single quotes before passing the identifier to SQLCMD so the substitution remains a valid T-SQL string literal:

```powershell
$ownerIdentifier = "owner@example.com"
$escapedOwnerIdentifier = $ownerIdentifier.Replace("'", "''")
sqlcmd -S <server> -d <database> -v LegacyContactOwnerIdentifierSqlEscaped="$escapedOwnerIdentifier" -i 002_backfill_contact_owners.sql
```

For example, `o'connor@example.com` is supplied as `o''connor@example.com` and is decoded by the T-SQL string literal back to the original identifier.

The script verifies that the identifier resolves to exactly one user, backfills every null `owner_id`, verifies the result, and makes `owner_id` non-null in one transaction. Take a database backup and validate the selected owner with the data owner before rollout.
