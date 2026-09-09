import { Briefcase, LoaderCircle, Mail, Phone, UserRound, X } from "lucide-react";

function ContactProfile({ contact, state, error, onRetry, onClose, isDarkMode, dialogRef, closeRef, onKeyDown }) {
  return (
    <div ref={dialogRef} className="fixed inset-0 z-50 flex items-center justify-center bg-black/55 p-4 backdrop-blur-sm" role="dialog" aria-modal="true" aria-labelledby="contact-profile-title" aria-busy={state === "loading"} onKeyDown={onKeyDown}>
      <div className={`w-full max-w-xl rounded-3xl p-6 shadow-2xl ${isDarkMode ? "bg-[#242B31] text-[#F7FAFC]" : "bg-white text-[#293241]"}`}>
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="rounded-full bg-[#D9EAF2] p-3 text-[#16425B]"><UserRound className="h-7 w-7" /></div>
            <div>
              <h2 id="contact-profile-title" className="text-2xl font-bold">{contact.firstName || "Contact"} {contact.lastName || ""}</h2>
              {contact.title && <p className="mt-1 flex items-center gap-2 text-[#60758A]"><Briefcase className="h-4 w-4" />{contact.title}</p>}
            </div>
          </div>
          <button ref={closeRef} type="button" onClick={onClose} aria-label="Close contact profile" className="rounded-full p-2 text-[#60758A] hover:bg-black/5"><X className="h-5 w-5" /></button>
        </div>

        <ProfileContent contact={contact} state={state} error={error} onRetry={onRetry} isDarkMode={isDarkMode} />

        <div className="mt-6 flex justify-end">
          <button type="button" onClick={onClose} className="rounded-full bg-[#16425B] px-5 py-2.5 font-semibold text-white">Close</button>
        </div>
      </div>
    </div>
  );
}

function ProfileContent({ contact, state, error, onRetry, isDarkMode }) {
  if (state === "loading") return <LoadingState />;
  if (state === "error") return <ErrorState error={error} onRetry={onRetry} />;
  return <ReadyState contact={contact} isDarkMode={isDarkMode} />;
}

function LoadingState() {
  return <div className="flex min-h-48 items-center justify-center gap-3" role="status"><LoaderCircle className="h-6 w-6 animate-spin" /> Loading contact profile...</div>;
}

function ErrorState({ error, onRetry }) {
  return (
    <div className="flex min-h-48 flex-col items-center justify-center gap-4 text-center" role="alert">
      <p>{error}</p>
      <button type="button" onClick={onRetry} className="rounded-full bg-[#16425B] px-5 py-2.5 font-semibold text-white">Try Again</button>
    </div>
  );
}

function ReadyState({ contact, isDarkMode }) {
  return (
    <>
      <ProfileSection title="Email addresses" icon={Mail} items={contact.emailAddresses} valueKey="email" empty="No email addresses" isDarkMode={isDarkMode} />
      <ProfileSection title="Phone numbers" icon={Phone} items={contact.phoneNumbers} valueKey="phoneNumber" empty="No phone numbers" isDarkMode={isDarkMode} />
    </>
  );
}

function ProfileSection({ title, icon: Icon, items, valueKey, empty, isDarkMode }) {
  const entries = items ?? [];

  return (
    <section className={`mt-6 rounded-2xl border p-4 ${isDarkMode ? "border-white/10" : "border-[#E7F1F6]"}`}>
      <h3 className="mb-3 flex items-center gap-2 font-semibold"><Icon className="h-5 w-5 text-[#EE6C4D]" />{title}</h3>
      {entries.length ? (
        <ul className="space-y-2">
          {entries.map((item, index) => (
            <li key={`${item[valueKey]}-${index}`} className="flex items-center justify-between gap-4">
              <span className="break-all">{item[valueKey]}</span>
              <span className={`shrink-0 rounded-full px-2.5 py-1 text-xs ${isDarkMode ? "bg-white/10 text-[#AFCBDD]" : "bg-[#EEF5F8] text-[#60758A]"}`}>{item.label}</span>
            </li>
          ))}
        </ul>
      ) : <p className="text-sm text-[#60758A]">{empty}</p>}
    </section>
  );
}

export default ContactProfile;
