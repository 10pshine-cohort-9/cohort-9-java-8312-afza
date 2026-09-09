import { useRef, useState } from "react";
import { ArrowLeft, CheckCircle2, Eye, EyeOff, Mail, Phone, UserRound, XCircle } from "lucide-react";
import { registerUser } from "../api/registrationApi";
import { getSession } from "../api/authApi";

const initialForm = {
  firstName: "",
  lastName: "",
  contactMethod: "email",
  email: "",
  phone: "",
  password: "",
  confirmPassword: "",
};

function Registration({ onBack, onRegistration }) {
  const [form, setForm] = useState(initialForm);
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const requestInProgress = useRef(false);

  const validate = () => {
    const nextErrors = {};
    if (!form.firstName.trim()) nextErrors.firstName = "First name is required";
    if (form.firstName.trim().length > 100) nextErrors.firstName = "First name must not exceed 100 characters";
    if (!form.lastName.trim()) nextErrors.lastName = "Last name is required";
    if (form.lastName.trim().length > 100) nextErrors.lastName = "Last name must not exceed 100 characters";

    if (form.contactMethod === "email") {
      if (!form.email.trim()) nextErrors.email = "Email is required";
      else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) nextErrors.email = "Enter a valid email address";
      else if (form.email.trim().length > 254) nextErrors.email = "Email must not exceed 254 characters";
    } else if (!/^\+?[1-9]\d{7,14}$/.test(form.phone.trim())) {
      nextErrors.phone = "Use 8 to 15 digits, optionally starting with +";
    }

    if (form.password.length < 8) nextErrors.password = "Password must be at least 8 characters";
    else if (new TextEncoder().encode(form.password).length > 72) nextErrors.password = "Password must not exceed 72 bytes when UTF-8 encoded";
    if (form.confirmPassword !== form.password) nextErrors.confirmPassword = "Passwords do not match";
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const updateField = ({ target: { name, value } }) => {
    setForm((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: undefined, identifier: undefined }));
    setFeedback(null);
  };

  const selectContactMethod = (contactMethod) => {
    setForm((current) => ({ ...current, contactMethod, email: "", phone: "" }));
    setErrors((current) => ({ ...current, email: undefined, phone: undefined, identifier: undefined }));
    setFeedback(null);
  };

  const submit = async (event) => {
    event.preventDefault();
    if (requestInProgress.current || !validate()) return;

    requestInProgress.current = true;
    setSubmitting(true);
    setFeedback(null);
    try {
      const payload = {
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.contactMethod === "email" ? form.email.trim() : null,
        phone: form.contactMethod === "phone" ? form.phone.trim() : null,
        password: form.password,
      };
      await registerUser(payload);
      const { data: profile } = await getSession({ suppressUnauthorizedHandler: true });
      onRegistration(profile);
    } catch (error) {
      const response = error.response;
      if (response?.status === 400 && response.data?.errors) {
        setErrors(response.data.errors);
        setFeedback({ type: "error", message: response.data.message || "Please correct the highlighted fields" });
      } else if (response?.status === 409) {
        setFeedback({ type: "error", message: response.data?.message || "An account already exists" });
      } else if (response?.status === 401) {
        setFeedback({
          type: "success",
          message: "If the provided contact information is eligible, registration has been accepted. Sign in with existing credentials if you already have an account.",
        });
      } else {
        setFeedback({ type: "error", message: "Registration could not be completed. Please try again." });
      }
    } finally {
      requestInProgress.current = false;
      setSubmitting(false);
    }
  };

  const inputClass = (name) =>
    `w-full rounded-xl border bg-[#F7FAFC] px-4 py-3 pl-11 text-[#293241] outline-none transition focus:ring-2 disabled:cursor-not-allowed disabled:opacity-60 ${
      errors[name] ? "border-[#EE6C4D] focus:ring-[#EE6C4D]/30" : "border-[#98C1D9]/60 focus:border-[#3D5A80] focus:ring-[#98C1D9]/50"
    }`;

  const fieldError = (name) => errors[name] && (
    <p id={`${name}-error`} className="mt-1.5 text-sm text-[#B9472D]" role="alert">{errors[name]}</p>
  );

  return (
    <main className="min-h-screen bg-[#E0FBFC] px-4 py-8 sm:py-12">
      <div className="mx-auto max-w-5xl overflow-hidden rounded-3xl bg-white shadow-2xl shadow-[#16425B]/15 md:grid md:grid-cols-[0.8fr_1.2fr]">
        <section className="flex flex-col justify-between bg-[#16425B] p-8 text-white sm:p-10">
          <div>
            <button type="button" onClick={onBack} className="mb-12 inline-flex items-center gap-2 rounded-lg text-[#E0FBFC] hover:text-white focus:outline-none focus:ring-2 focus:ring-[#98C1D9]" disabled={submitting}>
              <ArrowLeft className="h-4 w-4" aria-hidden="true" /> Back to login
            </button>
            <div className="mb-5 inline-flex rounded-2xl bg-[#98C1D9]/20 p-3"><UserRound className="h-8 w-8 text-[#E0FBFC]" aria-hidden="true" /></div>
            <h1 className="text-3xl font-bold">Create your account</h1>
            <p className="mt-3 leading-relaxed text-[#98C1D9]">Register with the contact method that works best for you.</p>
          </div>
          <p className="mt-12 text-sm text-[#98C1D9]">Your password is securely hashed before it is stored.</p>
        </section>

        <section className="p-6 sm:p-10">
          <form onSubmit={submit} noValidate className="space-y-5">
            <div className="grid gap-5 sm:grid-cols-2">
              <Field label="First name" name="firstName" error={fieldError("firstName")}>
                <UserRound className="field-icon" aria-hidden="true" />
                <input id="firstName" name="firstName" autoComplete="given-name" value={form.firstName} onChange={updateField} disabled={submitting} aria-invalid={Boolean(errors.firstName)} aria-describedby={errors.firstName ? "firstName-error" : undefined} className={inputClass("firstName")} />
              </Field>
              <Field label="Last name" name="lastName" error={fieldError("lastName")}>
                <UserRound className="field-icon" aria-hidden="true" />
                <input id="lastName" name="lastName" autoComplete="family-name" value={form.lastName} onChange={updateField} disabled={submitting} aria-invalid={Boolean(errors.lastName)} aria-describedby={errors.lastName ? "lastName-error" : undefined} className={inputClass("lastName")} />
              </Field>
            </div>

            <fieldset>
              <legend className="mb-2 text-sm font-semibold text-[#293241]">Register using</legend>
              <div className="grid grid-cols-2 rounded-xl bg-[#E0FBFC] p-1" role="group">
                {["email", "phone"].map((method) => (
                  <button key={method} type="button" aria-pressed={form.contactMethod === method} onClick={() => selectContactMethod(method)} disabled={submitting} className={`rounded-lg px-4 py-2.5 text-sm font-semibold capitalize transition ${form.contactMethod === method ? "bg-[#3D5A80] text-white shadow" : "text-[#16425B] hover:bg-white/60"}`}>{method}</button>
                ))}
              </div>
            </fieldset>

            {form.contactMethod === "email" ? (
              <Field label="Email address" name="email" error={fieldError("email") || fieldError("identifier")}>
                <Mail className="field-icon" aria-hidden="true" />
                <input id="email" type="email" name="email" autoComplete="email" placeholder="you@example.com" value={form.email} onChange={updateField} disabled={submitting} aria-invalid={Boolean(errors.email || errors.identifier)} aria-describedby={errors.email ? "email-error" : errors.identifier ? "identifier-error" : undefined} className={inputClass(errors.identifier ? "identifier" : "email")} />
              </Field>
            ) : (
              <Field label="Phone number" name="phone" error={fieldError("phone") || fieldError("identifier")}>
                <Phone className="field-icon" aria-hidden="true" />
                <input id="phone" type="tel" name="phone" autoComplete="tel" inputMode="tel" placeholder="+923001234567" value={form.phone} onChange={updateField} disabled={submitting} aria-invalid={Boolean(errors.phone || errors.identifier)} aria-describedby={errors.phone ? "phone-error" : errors.identifier ? "identifier-error" : undefined} className={inputClass(errors.identifier ? "identifier" : "phone")} />
              </Field>
            )}

            <Field label="Password" name="password" error={fieldError("password")}>
              <button type="button" onClick={() => setShowPassword((visible) => !visible)} className="absolute inset-y-0 right-0 z-10 flex w-11 items-center justify-center text-[#3D5A80]" aria-label={showPassword ? "Hide password" : "Show password"}>
                {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
              </button>
              <input id="password" type={showPassword ? "text" : "password"} name="password" autoComplete="new-password" value={form.password} onChange={updateField} disabled={submitting} aria-invalid={Boolean(errors.password)} aria-describedby={errors.password ? "password-error" : "password-help"} className={`${inputClass("password")} pl-4 pr-11`} />
              {!errors.password && <p id="password-help" className="mt-1.5 text-xs text-[#3D5A80]">Use at least 8 characters and no more than 72 UTF-8 bytes.</p>}
            </Field>

            <Field label="Confirm password" name="confirmPassword" error={fieldError("confirmPassword")}>
              <input id="confirmPassword" type="password" name="confirmPassword" autoComplete="new-password" value={form.confirmPassword} onChange={updateField} disabled={submitting} aria-invalid={Boolean(errors.confirmPassword)} aria-describedby={errors.confirmPassword ? "confirmPassword-error" : undefined} className={`${inputClass("confirmPassword")} pl-4`} />
            </Field>

            {feedback && (
              <div className={`flex items-start gap-2 rounded-xl border p-3 text-sm ${feedback.type === "success" ? "border-[#98C1D9] bg-[#E0FBFC] text-[#16425B]" : "border-[#EE6C4D]/50 bg-[#EE6C4D]/10 text-[#9C351E]"}`} role={feedback.type === "error" ? "alert" : "status"} aria-live="polite">
                {feedback.type === "success" ? <CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0" /> : <XCircle className="mt-0.5 h-5 w-5 shrink-0" />}{feedback.message}
              </div>
            )}

            <button type="submit" disabled={submitting} className="w-full rounded-full bg-[#EE6C4D] px-6 py-3 font-semibold text-white transition hover:bg-[#D95B3E] focus:outline-none focus:ring-2 focus:ring-[#EE6C4D]/50 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60">
              {submitting ? "Creating account…" : "Create account"}
            </button>
          </form>
        </section>
      </div>
    </main>
  );
}

function Field({ label, name, children, error }) {
  return (
    <div>
      <label htmlFor={name} className="mb-1.5 block text-sm font-semibold text-[#293241]">{label} <span className="text-[#EE6C4D]" aria-hidden="true">*</span></label>
      <div className="relative">{children}</div>
      {error}
    </div>
  );
}

export default Registration;
