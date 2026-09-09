import { useEffect, useRef, useState } from "react";
import { ArrowLeft, CheckCircle2, KeyRound, LogOut, Mail, Phone, UserRound, X } from "lucide-react";
import { changePassword } from "../api/authApi";

const emptyForm = { currentPassword: "", newPassword: "", confirmNewPassword: "" };

function Profile({ user, onBack, onLogout }) {
  const [showPasswordForm, setShowPasswordForm] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);
  const changePasswordTriggerRef = useRef(null);

  const closePasswordForm = () => {
    setShowPasswordForm(false);
    window.requestAnimationFrame(() => changePasswordTriggerRef.current?.focus());
  };

  const logout = async () => {
    if (loggingOut) return;
    setLoggingOut(true);
    await onLogout();
  };

  return (
    <main className="min-h-screen bg-[#E0FBFC] px-4 py-8 sm:py-12">
      <div className="mx-auto max-w-3xl overflow-hidden rounded-3xl bg-white shadow-2xl shadow-[#16425B]/15">
        <header className="bg-[#16425B] p-7 text-white sm:p-10">
          <button type="button" onClick={onBack} className="mb-8 inline-flex items-center gap-2 rounded-lg text-[#E0FBFC] focus:outline-none focus:ring-2 focus:ring-[#98C1D9]"><ArrowLeft className="h-4 w-4" /> Back to contacts</button>
          <div className="flex items-center gap-4"><div className="rounded-full bg-[#98C1D9]/20 p-4"><UserRound className="h-9 w-9" /></div><div><h1 className="text-3xl font-bold">Your profile</h1><p className="mt-1 text-[#98C1D9]">Account details and security</p></div></div>
        </header>
        <section className="p-7 sm:p-10">
          <dl className="grid gap-5 sm:grid-cols-2">
            <ProfileItem label="First name" value={user.firstName} icon={<UserRound />} />
            <ProfileItem label="Last name" value={user.lastName} icon={<UserRound />} />
            <ProfileItem label={user.email ? "Email address" : "Phone number"} value={user.email || user.phone} icon={user.email ? <Mail /> : <Phone />} />
          </dl>
          <div className="mt-10 flex flex-col gap-3 border-t border-[#98C1D9]/40 pt-7 sm:flex-row sm:justify-end">
            <button ref={changePasswordTriggerRef} type="button" onClick={() => setShowPasswordForm(true)} className="inline-flex items-center justify-center gap-2 rounded-full bg-[#3D5A80] px-5 py-3 font-semibold text-white hover:bg-[#16425B] focus:outline-none focus:ring-2 focus:ring-[#98C1D9]"><KeyRound className="h-5 w-5" /> Change password</button>
            <button type="button" onClick={logout} disabled={loggingOut} className="inline-flex items-center justify-center gap-2 rounded-full bg-[#EE6C4D] px-5 py-3 font-semibold text-white hover:bg-[#D95B3E] focus:outline-none focus:ring-2 focus:ring-[#EE6C4D]/50 disabled:opacity-60"><LogOut className="h-5 w-5" />{loggingOut ? "Logging out…" : "Logout"}</button>
          </div>
        </section>
      </div>
      {showPasswordForm && <ChangePasswordModal onClose={closePasswordForm} />}
    </main>
  );
}

function ProfileItem({ label, value, icon }) {
  return <div className="rounded-2xl bg-[#E0FBFC]/70 p-5"><dt className="flex items-center gap-2 text-sm font-semibold text-[#3D5A80]">{<span className="[&>svg]:h-4 [&>svg]:w-4">{icon}</span>}{label}</dt><dd className="mt-2 break-words text-lg font-semibold text-[#293241]">{value}</dd></div>;
}

function ChangePasswordModal({ onClose }) {
  const [form, setForm] = useState(emptyForm);
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const dialogRef = useRef(null);
  const firstInputRef = useRef(null);
  const requestInProgress = useRef(false);

  useEffect(() => { firstInputRef.current?.focus(); }, []);

  const close = () => { if (!submitting) onClose(); };
  const keyDown = (event) => {
    if (event.key === "Escape") { close(); return; }
    if (event.key !== "Tab") return;
    const items = [...dialogRef.current.querySelectorAll('button:not([disabled]), input:not([disabled])')];
    const first = items[0]; const last = items[items.length - 1];
    if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
    else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
  };

  const validate = () => {
    const next = {};
    if (!form.currentPassword) next.currentPassword = "Current password is required";
    if (form.newPassword.length < 8) next.newPassword = "New password must be at least 8 characters";
    else if (new TextEncoder().encode(form.newPassword).length > 72) next.newPassword = "New password must not exceed 72 bytes when UTF-8 encoded";
    if (!form.confirmNewPassword) next.confirmNewPassword = "Please confirm the new password";
    else if (form.confirmNewPassword !== form.newPassword) next.confirmNewPassword = "New passwords do not match";
    setErrors(next); return Object.keys(next).length === 0;
  };

  const submit = async (event) => {
    event.preventDefault();
    if (requestInProgress.current || !validate()) return;
    requestInProgress.current = true; setSubmitting(true); setFeedback(null);
    try {
      await changePassword(form);
      setForm(emptyForm); setErrors({}); setFeedback({ type: "success", message: "Password changed successfully" });
      window.setTimeout(onClose, 900);
    } catch (error) {
      if (error.response?.data?.errors) setErrors(error.response.data.errors);
      const serverMessage = error.response?.data?.message;
      const safeMessage = serverMessage && serverMessage !== "No message available"
        ? serverMessage
        : error.response?.status === 403
          ? "Your security session could not be verified. Refresh the page and try again."
          : "Password could not be changed. Please try again.";
      setFeedback({ type: "error", message: safeMessage });
    } finally { requestInProgress.current = false; setSubmitting(false); }
  };

  const update = ({ target: { name, value } }) => { setForm((current) => ({ ...current, [name]: value })); setErrors((current) => ({ ...current, [name]: undefined })); setFeedback(null); };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#293241]/70 p-4 backdrop-blur-sm" role="dialog" aria-modal="true" aria-labelledby="change-password-title" onKeyDown={keyDown}>
      <div ref={dialogRef} className="w-full max-w-lg rounded-3xl bg-white p-6 shadow-2xl sm:p-8">
        <div className="flex items-start justify-between"><div><h2 id="change-password-title" className="text-2xl font-bold text-[#293241]">Change password</h2><p className="mt-1 text-sm text-[#3D5A80]">Use at least 8 characters and no more than 72 UTF-8 bytes.</p></div><button type="button" onClick={close} disabled={submitting} aria-label="Close change password dialog" className="rounded-full p-2 text-[#3D5A80] hover:bg-[#E0FBFC]"><X className="h-5 w-5" /></button></div>
        <form onSubmit={submit} noValidate className="mt-6 space-y-4">
          {[{ name: "currentPassword", label: "Current password", autoComplete: "current-password", ref: firstInputRef }, { name: "newPassword", label: "New password", autoComplete: "new-password" }, { name: "confirmNewPassword", label: "Confirm new password", autoComplete: "new-password" }].map((field) => <PasswordField key={field.name} {...field} value={form[field.name]} error={errors[field.name]} onChange={update} disabled={submitting} />)}
          {feedback && <div className={`flex gap-2 rounded-xl border p-3 text-sm ${feedback.type === "success" ? "border-[#98C1D9] bg-[#E0FBFC] text-[#16425B]" : "border-[#EE6C4D]/50 bg-[#EE6C4D]/10 text-[#9C351E]"}`} role={feedback.type === "error" ? "alert" : "status"} aria-live="polite">{feedback.type === "success" && <CheckCircle2 className="h-5 w-5" />}{feedback.message}</div>}
          <div className="flex flex-col-reverse gap-3 pt-2 sm:flex-row sm:justify-end"><button type="reset" onClick={() => { setForm(emptyForm); setErrors({}); setFeedback(null); }} disabled={submitting} className="rounded-full bg-[#E0FBFC] px-5 py-2.5 font-semibold text-[#16425B] disabled:opacity-60">Reset</button><button type="button" onClick={close} disabled={submitting} className="rounded-full border border-[#98C1D9] px-5 py-2.5 font-semibold text-[#16425B] disabled:opacity-60">Cancel</button><button type="submit" disabled={submitting} className="rounded-full bg-[#EE6C4D] px-5 py-2.5 font-semibold text-white disabled:opacity-60">{submitting ? "Changing…" : "Change password"}</button></div>
        </form>
      </div>
    </div>
  );
}

function PasswordField({ name, label, value, error, onChange, disabled, autoComplete, ref: inputRef }) {
  return <div><label htmlFor={name} className="mb-1.5 block text-sm font-semibold text-[#293241]">{label}</label><input ref={inputRef} id={name} name={name} type="password" value={value} onChange={onChange} disabled={disabled} autoComplete={autoComplete} aria-invalid={Boolean(error)} aria-describedby={error ? `${name}-error` : undefined} className="w-full rounded-xl border border-[#98C1D9]/60 bg-[#F7FAFC] px-4 py-3 text-[#293241] outline-none focus:ring-2 focus:ring-[#98C1D9]/50 disabled:opacity-60" />{error && <p id={`${name}-error`} className="mt-1 text-sm text-[#B9472D]" role="alert">{error}</p>}</div>;
}

export default Profile;
