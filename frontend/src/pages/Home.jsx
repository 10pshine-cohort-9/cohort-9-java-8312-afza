import { useCallback, useEffect, useMemo, useState, useRef } from "react";
import { Toaster, toast } from "react-hot-toast";
import contactApi from "../api/contactApi";
import ContactForm from "../components/ContactForm";
import ContactList from "../components/ContactList";
import ContactProfile from "../components/ContactProfile";
import LoadingSpinner from "../components/LoadingSpinner";
import EmptyState from "../components/EmptyState";
import { Users, Plus, Moon, Sun, Trash2, X, CircleUserRound } from "lucide-react";

const requestTitles = () => contactApi.get("/contacts/titles");

function Home({ onProfile }) {
  const [contacts, setContacts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [filterTitle, setFilterTitle] = useState("");
  const [sortBy, setSortBy] = useState("name");
  const [titles, setTitles] = useState([]);
  const [titlesError, setTitlesError] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingContact, setEditingContact] = useState(null);
  const [isFormDirty, setIsFormDirty] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(false);
  const [contactToDelete, setContactToDelete] = useState(null);
  const [profileContact, setProfileContact] = useState(null);
  const [profileState, setProfileState] = useState("idle");
  const [profileError, setProfileError] = useState("");
  const [isDeleting, setIsDeleting] = useState(false);
  const formRef = useRef(null);
  const loadRequestId = useRef(0);
  const titleRequestId = useRef(0);
  const contactsVersion = useRef(0);
  const deleteInProgress = useRef(false);
  const deleteDialogRef = useRef(null);
  const deleteDialogInitialFocusRef = useRef(null);
  const deleteTriggerRef = useRef(null);
  const profileDialogRef = useRef(null);
  const profileCloseRef = useRef(null);
  const profileTriggerRef = useRef(null);
  const profileRequestId = useRef(0);
  const editFormData = useMemo(
    () =>
      editingContact
        ? {
            firstName: editingContact.firstName || "",
            lastName: editingContact.lastName || "",
            title: editingContact.title || "",
            emailAddresses: editingContact.emailAddresses || [],
            phoneNumbers: editingContact.phoneNumbers || [],
          }
        : null,
    [editingContact],
  );

  const loadContacts = useCallback(async (requestedPage = page) => {
    const requestId = ++loadRequestId.current;
    const requestContactsVersion = contactsVersion.current;
    setLoading(true);
    try {
      const response = await contactApi.get("/contacts", {
        params: {
          page: requestedPage,
          size: 9,
          search: searchTerm.trim(),
          title: filterTitle,
          sort:
            sortBy === "title"
              ? "title"
              : sortBy === "email"
                ? "email"
                : "firstName",
        },
      });
      if (
        requestId === loadRequestId.current &&
        requestContactsVersion === contactsVersion.current
      ) {
        if (response.data.totalPages > 0 && requestedPage >= response.data.totalPages) {
          setPage(response.data.totalPages - 1);
          return;
        }
        setContacts(response.data.content);
        setTotalPages(response.data.totalPages);
        setTotalElements(response.data.totalElements);
        setLoadError(false);
      }
    } catch (error) {
      console.error("Error loading contacts:", error);
      if (
        requestId === loadRequestId.current &&
        requestContactsVersion === contactsVersion.current
      ) {
        setLoadError(true);
        toast.error("Unable to load contacts. Please try again.");
      }
    } finally {
      if (
        requestId === loadRequestId.current &&
        requestContactsVersion === contactsVersion.current
      ) {
        setLoading(false);
      }
    }
  }, [filterTitle, page, searchTerm, sortBy]);

  const loadTitles = useCallback(async () => {
    const requestId = ++titleRequestId.current;
    setTitlesError(false);
    try {
      const response = await requestTitles();
      if (requestId === titleRequestId.current) {
        setTitles(response.data);
      }
    } catch (error) {
      console.error("Error loading contact titles:", error);
      if (requestId === titleRequestId.current) {
        setTitlesError(true);
      }
    }
  }, []);

  useEffect(() => {
    loadRequestId.current += 1;
    const timeoutId = setTimeout(loadContacts, searchTerm ? 300 : 0);
    return () => clearTimeout(timeoutId);
  }, [loadContacts, searchTerm]);

  useEffect(() => {
    const timeoutId = setTimeout(loadTitles, 0);
    return () => {
      clearTimeout(timeoutId);
      titleRequestId.current += 1;
    };
  }, [loadTitles]);

  useEffect(() => {
    if (!contactToDelete) {
      return undefined;
    }

    deleteDialogInitialFocusRef.current?.focus();

    return () => {
      if (deleteTriggerRef.current?.isConnected) {
        deleteTriggerRef.current.focus();
      }
    };
  }, [contactToDelete]);

  useEffect(() => {
    if (!profileContact) return undefined;
    profileCloseRef.current?.focus();
    return () => profileTriggerRef.current?.isConnected && profileTriggerRef.current.focus();
  }, [profileContact]);

  const changeSearch = (value) => {
    setSearchTerm(value);
    setPage(0);
  };

  const changeTitle = (value) => {
    setFilterTitle(value);
    setPage(0);
  };

  const changeSort = (value) => {
    setSortBy(value);
    setPage(0);
  };

  const saveContact = async (data) => {
    const contact = data;

    try {
      const response = await contactApi.post("/contacts", contact);
      contactsVersion.current += 1;
      setContacts((currentContacts) => {
        const contactIndex = currentContacts.findIndex(
          (currentContact) => currentContact.id === response.data.id,
        );

        if (contactIndex === -1) {
          return [...currentContacts, response.data];
        }

        return currentContacts.map((currentContact, index) =>
          index === contactIndex ? response.data : currentContact,
        );
      });
      void loadContacts();
      void loadTitles();
      toast.success("Contact added successfully!", {
        duration: 4000,
        position: "top-right",
        style: {
          background: "#16425B",
          color: "#fff",
          padding: "16px 20px",
          borderRadius: "8px",
        },
      });
      setIsFormDirty(false);
      setShowForm(false);
    } catch (error) {
      console.error("Error saving contact:", error);
      toast.error("Unable to add contact. Please try again.", {
        duration: 4000,
        position: "top-right",
        style: {
          background: "#EE6C4D",
          color: "#fff",
          padding: "16px 20px",
          borderRadius: "8px",
        },
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const updateContact = async (data) => {
    if (!editingContact) {
      setIsSubmitting(false);
      return;
    }

    const contact = data;

    try {
      const response = await contactApi.put(
        `/contacts/${editingContact.id}`,
        contact,
      );
      contactsVersion.current += 1;
      setContacts((currentContacts) =>
        currentContacts.map((currentContact) =>
          currentContact.id === response.data.id
            ? response.data
            : currentContact,
        ),
      );
      void loadContacts();
      void loadTitles();
      toast.success("Contact updated successfully!", {
        duration: 4000,
        position: "top-right",
        style: {
          background: "#16425B",
          color: "#fff",
          padding: "16px 20px",
          borderRadius: "8px",
        },
      });
      setEditingContact(null);
      setIsFormDirty(false);
      setShowForm(false);
    } catch (error) {
      console.error("Error updating contact:", error);
      toast.error("Unable to update contact. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const editContact = (contact) => {
    setEditingContact(contact);
    setIsFormDirty(false);
    setShowForm(true);
    setTimeout(scrollToForm, 100);
  };

  const loadContactProfile = async (contactId) => {
    const requestId = ++profileRequestId.current;
    setProfileState("loading");
    setProfileError("");
    try {
      const response = await contactApi.get(`/contacts/${contactId}`);
      if (requestId === profileRequestId.current) {
        setProfileContact(response.data);
        setProfileState("ready");
      }
    } catch (error) {
      if (requestId === profileRequestId.current) {
        setProfileState("error");
        setProfileError(
          error.response?.status === 404
            ? "This contact no longer exists."
            : "Unable to load this contact. Please try again.",
        );
      }
    }
  };

  const viewContact = (contact) => {
    profileTriggerRef.current = document.activeElement;
    setProfileContact(contact);
    void loadContactProfile(contact.id);
  };

  const closeContactProfile = () => {
    profileRequestId.current += 1;
    setProfileContact(null);
    setProfileState("idle");
    setProfileError("");
  };

  const handleProfileKeyDown = (event) => {
    if (event.key === "Escape") closeContactProfile();
    if (event.key !== "Tab") return;
    const elements = Array.from(profileDialogRef.current?.querySelectorAll('button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])') ?? []);
    if (!elements.length) return;
    if (event.shiftKey && document.activeElement === elements[0]) { event.preventDefault(); elements.at(-1).focus(); }
    else if (!event.shiftKey && document.activeElement === elements.at(-1)) { event.preventDefault(); elements[0].focus(); }
  };

  const cancelEdit = () => {
    if (isFormDirty && !window.confirm("Discard your unsaved contact changes?")) return;
    setEditingContact(null);
    setIsFormDirty(false);
    setShowForm(false);
  };

  const openDeleteDialog = (contact) => {
    deleteTriggerRef.current = document.activeElement;
    setContactToDelete(contact);
  };

  const handleDeleteDialogKeyDown = (event) => {
    if (event.key !== "Tab") {
      return;
    }

    const focusableElements = Array.from(
      deleteDialogRef.current?.querySelectorAll(
        'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
      ) ?? [],
    );

    if (focusableElements.length === 0) {
      event.preventDefault();
      return;
    }

    const firstElement = focusableElements[0];
    const lastElement = focusableElements[focusableElements.length - 1];

    if (event.shiftKey && document.activeElement === firstElement) {
      event.preventDefault();
      lastElement.focus();
    } else if (!event.shiftKey && document.activeElement === lastElement) {
      event.preventDefault();
      firstElement.focus();
    }
  };

  const deleteContact = async () => {
    if (!contactToDelete || isDeleting || deleteInProgress.current) {
      return;
    }

    deleteInProgress.current = true;
    setIsDeleting(true);

    try {
      await contactApi.delete(`/contacts/${contactToDelete.id}`);
      contactsVersion.current += 1;
      await loadContacts(page);
      void loadTitles();

      if (editingContact?.id === contactToDelete.id) {
        setEditingContact(null);
        setShowForm(false);
      }

      toast.success("Contact deleted successfully!", {
        duration: 4000,
        position: "top-right",
        style: {
          background: "#16425B",
          color: "#fff",
          padding: "16px 20px",
          borderRadius: "8px",
        },
      });
      setContactToDelete(null);
    } catch (error) {
      console.error("Error deleting contact:", error);
      const message =
        error.response?.status === 404
          ? "This contact no longer exists. Refresh and try again."
          : error.response
            ? "Unable to delete contact. Please try again."
            : "Unable to reach the server. Check your connection and try again.";
      toast.error(message);
    } finally {
      deleteInProgress.current = false;
      setIsDeleting(false);
    }
  };

  const scrollToForm = () => {
    formRef.current?.scrollIntoView({ behavior: "smooth", block: "center" });
  };

  const handleProfile = () => {
    if (showForm && isFormDirty && !window.confirm("Discard your unsaved contact changes?")) return;
    onProfile();
  };

  const toggleContactForm = () => {
    if (showForm && isFormDirty && !window.confirm("Discard your unsaved contact changes?")) return;

    setEditingContact(null);
    setIsFormDirty(false);
    setShowForm((current) => !current);
    if (!showForm) setTimeout(scrollToForm, 100);
  };

  return (
    <div
      className={`min-h-screen transition-colors duration-300 ${
        isDarkMode ? "bg-[#171C21]" : "bg-[#EEF1F3]"
      }`}
    >
      <Toaster />
      
      {/* Header */}
      <div className={`sticky top-0 z-10 shadow-sm transition-colors duration-300 ${isDarkMode ? "bg-[#20262C]" : "bg-[#16425B]"}`}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-5">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="bg-[#E0FBFC]/15 p-2.5 rounded-xl">
                <Users className="h-7 w-7 text-white" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-[#E0FBFC]">
                  Contact Management
                </h1>
                <p className="text-sm text-[#98C1D9] flex items-center gap-1.5">
                  Stay connected, stay organized
                </p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={handleProfile}
                className="flex items-center gap-2 rounded-full border border-[#98C1D9]/60 px-4 py-2 text-sm font-semibold text-[#E0FBFC] transition-colors hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-[#98C1D9]"
              >
                <CircleUserRound className="h-4 w-4" aria-hidden="true" /> Profile
              </button>
              <button
                type="button"
                onClick={() => setIsDarkMode((currentMode) => !currentMode)}
                className="flex h-9 w-9 items-center justify-center rounded-full bg-white/10 text-[#E0FBFC] hover:bg-white/20 focus:outline-none focus:ring-2 focus:ring-[#98C1D9] transition-all duration-200"
                aria-label={isDarkMode ? "Switch to light mode" : "Switch to dark mode"}
                aria-pressed={isDarkMode}
              >
                {isDarkMode ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
              </button>
              <button
                onClick={toggleContactForm}
                className="flex items-center gap-2 px-5 py-2.5 bg-[#EE6C4D] text-white rounded-full font-semibold hover:bg-[#F07A5E] hover:-translate-y-0.5 hover:shadow-lg transition-all duration-200"
              >
                <Plus className="h-5 w-5" />
                {showForm ? "Close Form" : "Add Contact"}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Add Contact Form */}
        <div ref={formRef}>
          {showForm && (
            <div
              className={`mb-10 rounded-2xl p-8 shadow-lg animate-fade-in-up transition-all duration-300 ${
                isDarkMode
                  ? "bg-[#242B31] shadow-black/25"
                  : "bg-white shadow-[#16425B]/10"
              }`}
            >
              <div className="flex items-center gap-3 mb-7">
                <h2 className={`text-xl font-bold ${isDarkMode ? "text-[#F7FAFC]" : "text-[#16425B]"}`}>
                  {editingContact ? "Edit Contact" : "Add New Contact"}
                </h2>
                <span className={`text-sm ml-auto ${isDarkMode ? "text-[#AFCBDD]" : "text-[#60758A]"}`}>
                   Fill in the details below
                </span>
              </div>
              <ContactForm
                key={editingContact?.id || "new"}
                onSave={editingContact ? updateContact : saveContact}
                isSubmitting={isSubmitting}
                setIsSubmitting={setIsSubmitting}
                initialData={editFormData}
                onCancel={cancelEdit}
                isDarkMode={isDarkMode}
                onDirtyChange={setIsFormDirty}
              />
            </div>
          )}
        </div>

        {/* Contact List */}
        {loading && contacts.length === 0 ? (
          <LoadingSpinner isDarkMode={isDarkMode} />
        ) : loadError ? (
          <>
            {contacts.length > 0 && (
              <ContactList
                contacts={contacts}
                onView={viewContact}
                onEdit={editContact}
                onDelete={openDeleteDialog}
                isDarkMode={isDarkMode}
                searchTerm={searchTerm}
                onSearchChange={changeSearch}
                sortBy={sortBy}
                onSortChange={changeSort}
                filterTitle={filterTitle}
                onTitleChange={changeTitle}
                titles={titles}
                titlesError={titlesError}
                onRetryTitles={loadTitles}
                page={page}
                totalPages={totalPages}
                totalElements={totalElements}
                onPageChange={setPage}
              />
            )}
            <div className="rounded-lg border border-[#EE6C4D] bg-white p-6 text-center">
              <p className="text-[#293241]">Unable to load contacts.</p>
              <button
                type="button"
                onClick={() => void loadContacts()}
                className="mt-4 rounded-lg bg-[#16425B] px-4 py-2 font-semibold text-white hover:bg-[#3D5A80]"
              >
                Try Again
              </button>
            </div>
          </>
        ) : contacts.length === 0 && !searchTerm && !filterTitle && !titlesError ? (
          <EmptyState onAddContact={() => setShowForm(true)} isDarkMode={isDarkMode} />
        ) : (
          <ContactList
            contacts={contacts}
            onView={viewContact}
            onEdit={editContact}
            onDelete={openDeleteDialog}
            isDarkMode={isDarkMode}
            searchTerm={searchTerm}
            onSearchChange={changeSearch}
            sortBy={sortBy}
            onSortChange={changeSort}
            filterTitle={filterTitle}
            onTitleChange={changeTitle}
            titles={titles}
            titlesError={titlesError}
            onRetryTitles={loadTitles}
            page={page}
            totalPages={totalPages}
            totalElements={totalElements}
            onPageChange={setPage}
          />
        )}
      </div>

      {profileContact && <ContactProfile contact={profileContact} state={profileState} error={profileError} onRetry={() => void loadContactProfile(profileContact.id)} onClose={closeContactProfile} isDarkMode={isDarkMode} dialogRef={profileDialogRef} closeRef={profileCloseRef} onKeyDown={handleProfileKeyDown} />}

      {contactToDelete && (
        <div
          ref={deleteDialogRef}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/55 p-4 backdrop-blur-sm animate-fade-in-up"
          role="dialog"
          aria-modal="true"
          aria-labelledby="delete-contact-title"
          onKeyDown={handleDeleteDialogKeyDown}
        >
          <div
            className={`w-full max-w-md rounded-2xl p-6 shadow-2xl ${
              isDarkMode ? "bg-[#242B31] text-[#F7FAFC]" : "bg-white text-[#293241]"
            }`}
          >
            <div className="flex items-start gap-4">
              <div className="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-full bg-[#FCE9E4] text-[#D95D40]">
                <Trash2 className="h-5 w-5" />
              </div>
              <div className="min-w-0 flex-1">
                <h2 id="delete-contact-title" className="text-xl font-bold">
                  Delete contact?
                </h2>
                <p className={`mt-2 ${isDarkMode ? "text-[#B7C0C7]" : "text-[#60758A]"}`}>
                  Are you sure you want to delete{" "}
                  <span className="font-semibold">
                    {contactToDelete.firstName} {contactToDelete.lastName}
                  </span>
                  ? This action cannot be undone.
                </p>
              </div>
              <button
                ref={deleteDialogInitialFocusRef}
                type="button"
                onClick={() => setContactToDelete(null)}
                disabled={isDeleting}
                className="rounded-full p-2 text-[#60758A] hover:bg-black/5 disabled:cursor-not-allowed disabled:opacity-50"
                aria-label="Close delete confirmation"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="mt-7 flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setContactToDelete(null)}
                disabled={isDeleting}
                className={`rounded-full px-5 py-2.5 font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-50 ${
                  isDarkMode ? "bg-white/10 hover:bg-white/15" : "bg-[#E7EDF0] hover:bg-[#DCE5E9]"
                }`}
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={deleteContact}
                disabled={isDeleting}
                className="flex min-w-24 items-center justify-center rounded-full bg-[#EE6C4D] px-5 py-2.5 font-semibold text-white hover:bg-[#D95D40] disabled:cursor-not-allowed disabled:opacity-60 transition-colors"
              >
                {isDeleting ? "Deleting..." : "Delete"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Home;
