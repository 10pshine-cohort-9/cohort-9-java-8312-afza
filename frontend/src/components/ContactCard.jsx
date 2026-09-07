import { Mail, Phone, Briefcase, Eye, Pencil, Trash2 } from "lucide-react";

function ContactCard({ contact, onView, onEdit, onDelete, isDarkMode }) {
  const getInitials = (firstName, lastName) => {
    const first = firstName?.charAt(0) || "";
    const last = lastName?.charAt(0) || "";
    return (first + last).toUpperCase() || "?";
  };

  const initials = getInitials(contact.firstName, contact.lastName);

  return (
    <div
      className={`group rounded-2xl overflow-hidden hover:-translate-y-1.5 transition-all duration-300 ${
        isDarkMode
          ? "bg-[#242B31] shadow-md shadow-black/25 hover:shadow-xl hover:shadow-black/30"
          : "bg-white shadow-md shadow-[#16425B]/8 hover:shadow-xl hover:shadow-[#16425B]/14"
      }`}
    >
      <div className="p-6">
        <div className="flex items-start gap-5">
          {/* Avatar */}
          <div className="bg-[#D9EAF2] w-14 h-14 rounded-full flex items-center justify-center text-[#16425B] text-xl font-bold flex-shrink-0 transition-transform duration-300 group-hover:scale-105">
            {initials}
          </div>

          {/* Contact Info */}
          <div className="flex-1 min-w-0">
            <h3 className={`text-lg font-bold truncate ${isDarkMode ? "text-[#E0FBFC]" : "text-[#293241]"}`}>
              {contact.firstName} {contact.lastName}
            </h3>
            {contact.title && (
              <p className={`text-sm flex items-center gap-1.5 mt-1.5 ${isDarkMode ? "text-[#B7C0C7]" : "text-[#60758A]"}`}>
                <Briefcase className="h-4 w-4 text-[#EE6C4D]" />
                {contact.title}
              </p>
            )}
          </div>
          <div className="flex items-center gap-2">
            <button type="button" onClick={() => onView(contact)} className="flex h-9 w-9 items-center justify-center rounded-full bg-[#E0FBFC] text-[#16425B] focus:outline-none focus:ring-2 focus:ring-[#98C1D9]" aria-label={`View ${contact.firstName} ${contact.lastName || ""}`.trim()}><Eye className="h-4 w-4" /></button>
            <button
              type="button"
              onClick={() => onEdit(contact)}
              className="flex items-center gap-1.5 rounded-full bg-[#EE6C4D] px-3 py-2 text-sm font-semibold text-white hover:bg-[#D95D40] hover:-translate-y-0.5 focus:outline-none focus:ring-2 focus:ring-[#EE6C4D]/40 transition-all duration-200"
              aria-label={`Edit ${contact.firstName} ${contact.lastName || ""}`.trim()}
            >
              <Pencil className="h-4 w-4" />
              Edit
            </button>
            <button
              type="button"
              onClick={() => onDelete(contact)}
              className={`flex h-9 w-9 items-center justify-center rounded-full focus:outline-none focus:ring-2 focus:ring-[#EE6C4D]/40 transition-all duration-200 ${
                isDarkMode
                  ? "bg-white/10 text-[#EE6C4D] hover:bg-[#EE6C4D] hover:text-white"
                  : "bg-[#FCE9E4] text-[#D95D40] hover:bg-[#EE6C4D] hover:text-white"
              }`}
              aria-label={`Delete ${contact.firstName} ${contact.lastName || ""}`.trim()}
            >
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        </div>

        {/* Contact Details */}
        <div className={`mt-5 space-y-3 pt-4 border-t ${isDarkMode ? "border-white/10" : "border-[#E7F1F6]"}`}>
          {contact.emailAddresses?.length > 0 && (
            <div className={`flex items-center gap-2.5 text-sm group/item ${isDarkMode ? "text-[#E0FBFC]" : "text-[#293241]"}`}>
              <Mail className="h-4 w-4 text-[#EE6C4D] flex-shrink-0" />
              <span className="truncate">{contact.emailAddresses[0].email}</span>
              <span className={`text-xs ml-auto px-2 py-0.5 rounded-full ${isDarkMode ? "text-[#AFCBDD] bg-white/10" : "text-[#60758A] bg-[#EEF5F8]"}`}>
                {contact.emailAddresses[0].label}
              </span>
            </div>
          )}
          {contact.phoneNumbers?.length > 0 && (
            <div className={`flex items-center gap-2.5 text-sm group/item ${isDarkMode ? "text-[#E0FBFC]" : "text-[#293241]"}`}>
              <Phone className="h-4 w-4 text-[#EE6C4D] flex-shrink-0" />
              <span className="truncate">{contact.phoneNumbers[0].phoneNumber}</span>
              <span className={`text-xs ml-auto px-2 py-0.5 rounded-full ${isDarkMode ? "text-[#AFCBDD] bg-white/10" : "text-[#60758A] bg-[#EEF5F8]"}`}>
                {contact.phoneNumbers[0].label}
              </span>
            </div>
          )}
        </div>
      </div>

    </div>
  );
}

export default ContactCard;
