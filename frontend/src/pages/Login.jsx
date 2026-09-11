import { useRef, useState } from "react";
import { Eye, EyeOff, KeyRound, LogIn, UserRound, XCircle } from "lucide-react";
import { login } from "../api/authApi";

function Login({ onLogin, onRegister }) {
  const [form, setForm] = useState({ identifier: "", password: "" });
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const requestInProgress = useRef(false);

  const validate = () => {
    const next = {};
    if (!form.identifier.trim()) next.identifier = "Email or phone number is required";
    else if (form.identifier.trim().length > 254) next.identifier = "Email or phone number is too long";
    if (!form.password) next.password = "Password is required";
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const submit = async (event) => {
    event.preventDefault();
    if (requestInProgress.current || !validate()) return;
    requestInProgress.current = true;
    setSubmitting(true);
    setMessage("");
    try {
      const { data } = await login({ identifier: form.identifier.trim(), password: form.password });
      onLogin(data);
    } catch (error) {
      const response = error.response;
      if (response?.status === 400 && response.data?.errors) {
        setErrors(response.data.errors);
        setMessage(response.data.message || "Please correct the highlighted fields");
      } else {
        setMessage(
          response?.status === 401
            ? "Email/phone number or password is incorrect"
            : "Login is unavailable. Please try again.",
        );
      }
    } finally {
      requestInProgress.current = false;
      setSubmitting(false);
    }
  };

  const update = ({ target: { name, value } }) => {
    setForm((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: undefined }));
    setMessage("");
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-[#E0FBFC] px-4 py-10">
      <div className="grid w-full max-w-4xl overflow-hidden rounded-3xl bg-white shadow-2xl shadow-[#16425B]/15 md:grid-cols-2">
        <section className="flex flex-col justify-between bg-[#16425B] p-8 text-white sm:p-10">
          <div><div className="mb-6 inline-flex rounded-2xl bg-[#98C1D9]/20 p-3"><UserRound className="h-8 w-8" aria-hidden="true" /></div><h1 className="text-3xl font-bold">Welcome back</h1><p className="mt-3 leading-relaxed text-[#98C1D9]">Sign in to manage your contacts and account.</p></div>
          <p className="mt-12 text-sm text-[#98C1D9]">Your session is protected and expires automatically.</p>
        </section>
        <section className="p-7 sm:p-10">
          <form onSubmit={submit} noValidate className="space-y-5">
            <h2 className="text-2xl font-bold text-[#293241]">Sign in</h2>
            <LoginField label="Email or phone number" name="identifier" error={errors.identifier}>
              <UserRound className="field-icon" aria-hidden="true" />
              <input id="identifier" name="identifier" autoComplete="username" value={form.identifier} onChange={update} disabled={submitting} aria-invalid={Boolean(errors.identifier)} aria-describedby={errors.identifier ? "identifier-error" : undefined} className="w-full rounded-xl border border-[#98C1D9]/60 bg-[#F7FAFC] px-4 py-3 pl-11 text-[#293241] outline-none focus:border-[#3D5A80] focus:ring-2 focus:ring-[#98C1D9]/50 disabled:opacity-60" />
            </LoginField>
            <LoginField label="Password" name="password" error={errors.password}>
              <KeyRound className="field-icon" aria-hidden="true" />
              <input id="password" name="password" type={showPassword ? "text" : "password"} autoComplete="current-password" value={form.password} onChange={update} disabled={submitting} aria-invalid={Boolean(errors.password)} aria-describedby={errors.password ? "password-error" : undefined} className="w-full rounded-xl border border-[#98C1D9]/60 bg-[#F7FAFC] px-11 py-3 text-[#293241] outline-none focus:border-[#3D5A80] focus:ring-2 focus:ring-[#98C1D9]/50 disabled:opacity-60" />
              <button type="button" onClick={() => setShowPassword((value) => !value)} className="absolute inset-y-0 right-0 flex w-11 items-center justify-center text-[#3D5A80]" aria-label={showPassword ? "Hide password" : "Show password"}>{showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}</button>
            </LoginField>
            {message && <div className="flex gap-2 rounded-xl border border-[#EE6C4D]/50 bg-[#EE6C4D]/10 p-3 text-sm text-[#9C351E]" role="alert"><XCircle className="h-5 w-5 shrink-0" />{message}</div>}
            <button type="submit" disabled={submitting} className="flex w-full items-center justify-center gap-2 rounded-full bg-[#EE6C4D] px-6 py-3 font-semibold text-white hover:bg-[#D95B3E] focus:outline-none focus:ring-2 focus:ring-[#EE6C4D]/50 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60"><LogIn className="h-5 w-5" />{submitting ? "Signing in…" : "Sign in"}</button>
            <p className="text-center text-sm text-[#3D5A80]">Need an account? <button type="button" onClick={onRegister} disabled={submitting} className="font-semibold text-[#16425B] underline-offset-4 hover:underline">Register</button></p>
          </form>
        </section>
      </div>
    </main>
  );
}

function LoginField({ label, name, error, children }) {
  return <div><label htmlFor={name} className="mb-1.5 block text-sm font-semibold text-[#293241]">{label}</label><div className="relative">{children}</div>{error && <p id={`${name}-error`} className="mt-1.5 text-sm text-[#B9472D]" role="alert">{error}</p>}</div>;
}

export default Login;
