import { useEffect, useRef, useState } from "react";
import { Briefcase, Mail, Phone, Plus, Trash2, User, X } from "lucide-react";

const newEmail = () => ({ email: "", label: "Personal" });
const newPhone = () => ({ phoneNumber: "", label: "Personal" });
const emptyFormData = () => ({ firstName: "", lastName: "", title: "", emailAddresses: [newEmail()], phoneNumbers: [newPhone()] });
const normalize = (data) => data ? {
  firstName: data.firstName || "", lastName: data.lastName || "", title: data.title || "",
  emailAddresses: data.emailAddresses?.length ? data.emailAddresses.map(({ email, label }) => ({ email: email || "", label: label || "Personal" })) : [newEmail()],
  phoneNumbers: data.phoneNumbers?.length ? data.phoneNumbers.map(({ phoneNumber, label }) => ({ phoneNumber: phoneNumber || "", label: label || "Personal" })) : [newPhone()],
} : emptyFormData();

function ContactForm({ onSave, isSubmitting, setIsSubmitting, initialData, onCancel, isDarkMode, onDirtyChange }) {
  const [formData, setFormData] = useState(() => normalize(initialData));
  const [errors, setErrors] = useState({});
  const submissionInProgress = useRef(false);
  const initialFormData = useRef(normalize(initialData));

  useEffect(() => {
    onDirtyChange(JSON.stringify(formData) !== JSON.stringify(initialFormData.current));
  }, [formData, onDirtyChange]);

  const validate = () => {
    const next = {};
    if (!formData.firstName.trim()) next.firstName = "First name is required";
    formData.emailAddresses.forEach((item, index) => {
      if (!item.email.trim()) return;
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(item.email)) next[`email-${index}`] = "Enter a valid email address";
      if (!item.label.trim()) next[`email-label-${index}`] = "Label is required";
    });
    formData.phoneNumbers.forEach((item, index) => {
      if (!item.phoneNumber.trim()) return;
      if (!/^\+?[0-9]{8,15}$/.test(item.phoneNumber.trim())) next[`phone-${index}`] = "Use 8 to 15 digits, optionally starting with +";
      if (!item.label.trim()) next[`phone-label-${index}`] = "Label is required";
    });
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const setField = ({ target: { name, value } }) => {
    setFormData((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: undefined }));
  };
  const setItem = (collection, index, field, value) => setFormData((current) => ({ ...current, [collection]: current[collection].map((item, itemIndex) => itemIndex === index ? { ...item, [field]: value } : item) }));
  const addItem = (collection) => setFormData((current) => ({ ...current, [collection]: [...current[collection], collection === "emailAddresses" ? newEmail() : newPhone()] }));
  const removeItem = (collection, index) => setFormData((current) => ({ ...current, [collection]: current[collection].filter((_, itemIndex) => itemIndex !== index) }));
  const submit = async (event) => {
    event.preventDefault();
    if (isSubmitting || submissionInProgress.current || !validate()) return;
    submissionInProgress.current = true;
    setIsSubmitting(true);
    try { await onSave({ ...formData, firstName: formData.firstName.trim(), lastName: formData.lastName.trim(), title: formData.title.trim(), emailAddresses: formData.emailAddresses.filter((item) => item.email.trim()).map((item) => ({ email: item.email.trim(), label: item.label.trim() })), phoneNumbers: formData.phoneNumbers.filter((item) => item.phoneNumber.trim()).map((item) => ({ phoneNumber: item.phoneNumber.trim(), label: item.label.trim() })) }); }
    finally { submissionInProgress.current = false; }
  };
  const error = (key) => errors[key] && <p className="mt-1 text-sm text-[#EE6C4D]" role="alert"><X className="mr-1 inline h-4 w-4" />{errors[key]}</p>;

  return <form onSubmit={submit} noValidate className={`space-y-6 ${isDarkMode ? "[&_label]:text-[#E8ECEF] [&_input]:bg-[#1B2025] [&_input]:text-[#F7FAFC] [&_select]:bg-[#1B2025] [&_select]:text-[#F7FAFC]" : ""}`}>
    <div className="grid gap-5 md:grid-cols-2"><TextField id="firstName" label="First Name" required icon={User} value={formData.firstName} onChange={setField} disabled={isSubmitting} /><TextField id="lastName" label="Last Name" icon={User} value={formData.lastName} onChange={setField} disabled={isSubmitting} /></div>{error("firstName")}
    <TextField id="title" label="Title" icon={Briefcase} value={formData.title} onChange={setField} disabled={isSubmitting} />
    <Collection title="Email addresses" icon={Mail} onAdd={() => addItem("emailAddresses")} disabled={isSubmitting}>{formData.emailAddresses.map((item, index) => <MethodRow key={`email-${index}`} value={item.email} label={item.label} type="email" id={`email-${index}`} placeholder="name@example.com" labels={["Work", "Personal", "Other"]} onValue={(value) => setItem("emailAddresses", index, "email", value)} onLabel={(value) => setItem("emailAddresses", index, "label", value)} onRemove={() => removeItem("emailAddresses", index)} canRemove={formData.emailAddresses.length > 1} disabled={isSubmitting} valueError={error(`email-${index}`)} labelError={error(`email-label-${index}`)} />)}</Collection>
    <Collection title="Phone numbers" icon={Phone} onAdd={() => addItem("phoneNumbers")} disabled={isSubmitting}>{formData.phoneNumbers.map((item, index) => <MethodRow key={`phone-${index}`} value={item.phoneNumber} label={item.label} type="tel" id={`phone-${index}`} placeholder="+923001234567" labels={["Work", "Home", "Personal", "Other"]} onValue={(value) => setItem("phoneNumbers", index, "phoneNumber", value)} onLabel={(value) => setItem("phoneNumbers", index, "label", value)} onRemove={() => removeItem("phoneNumbers", index)} canRemove={formData.phoneNumbers.length > 1} disabled={isSubmitting} valueError={error(`phone-${index}`)} labelError={error(`phone-label-${index}`)} />)}</Collection>
    <div className="flex gap-4 pt-4"><button type="submit" disabled={isSubmitting} className="flex-1 rounded-full bg-[#16425B] px-6 py-3 font-semibold text-white disabled:opacity-50">{isSubmitting ? (initialData ? "Updating..." : "Saving...") : (initialData ? "Update Contact" : "Save Contact")}</button><button type="button" onClick={initialData ? onCancel : () => { setFormData(emptyFormData()); setErrors({}); }} disabled={isSubmitting} className="rounded-full bg-[#E7F1F6] px-6 py-3 font-semibold text-[#293241] disabled:opacity-50">{initialData ? "Cancel" : "Reset"}</button></div>
  </form>;
}

function TextField({ id, label, icon: Icon, required, ...props }) { return <div><label htmlFor={id} className="mb-1.5 block text-sm font-semibold text-[#293241]">{label}{required && <span className="text-[#EE6C4D]"> *</span>}</label><div className="relative"><Icon className="absolute left-4 top-3.5 h-5 w-5 text-[#3D5A80]" aria-hidden="true" /><input id={id} name={id} className="w-full rounded-xl bg-[#F1F6F8] px-4 py-3 pl-11 outline-none focus:ring-2 focus:ring-[#98C1D9]/60" {...props} /></div></div>; }
function Collection({ title, icon: Icon, onAdd, disabled, children }) { return <fieldset className="space-y-3 rounded-2xl border border-[#98C1D9]/40 p-4"><legend className="px-2 font-semibold text-[#293241]"><Icon className="mr-2 inline h-5 w-5 text-[#EE6C4D]" />{title}</legend>{children}<button type="button" onClick={onAdd} disabled={disabled} className="inline-flex items-center gap-2 rounded-full bg-[#E0FBFC] px-4 py-2 text-sm font-semibold text-[#16425B]"><Plus className="h-4 w-4" /> Add another</button></fieldset>; }
function MethodRow({ value, label, type, id, placeholder, labels, onValue, onLabel, onRemove, canRemove, disabled, valueError, labelError }) { return <div className="grid items-start gap-3 sm:grid-cols-[minmax(0,1fr)_10rem_2.5rem]"><div><label htmlFor={id} className="sr-only">{type === "email" ? "Email address" : "Phone number"}</label><input id={id} type={type} value={value} placeholder={placeholder} onChange={(event) => onValue(event.target.value)} disabled={disabled} className="w-full rounded-xl bg-[#F1F6F8] px-4 py-3 outline-none focus:ring-2 focus:ring-[#98C1D9]/60" />{valueError}</div><div><label htmlFor={`${id}-label`} className="sr-only">Label</label><select id={`${id}-label`} value={label} onChange={(event) => onLabel(event.target.value)} disabled={disabled} className="w-full rounded-xl bg-[#F1F6F8] px-3 py-3 outline-none focus:ring-2 focus:ring-[#98C1D9]/60">{labels.map((option) => <option key={option}>{option}</option>)}</select>{labelError}</div><button type="button" onClick={onRemove} disabled={disabled || !canRemove} aria-label="Remove entry" className="flex h-11 w-10 items-center justify-center rounded-full text-[#D95D40] hover:bg-[#FCE9E4] disabled:invisible"><Trash2 className="h-4 w-4" /></button></div>; }

export default ContactForm;
