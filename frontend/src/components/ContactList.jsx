import { useMemo } from "react";
import ContactCard from "./ContactCard";
import { Search, Filter, ArrowUpDown, X } from "lucide-react";

function ContactList({
  contacts,
  onView,
  onEdit,
  onDelete,
  isDarkMode,
  searchTerm,
  onSearchChange,
  sortBy,
  onSortChange,
  filterTitle,
  onTitleChange,
  titles,
  titlesError,
  onRetryTitles,
  page,
  totalPages,
  totalElements,
  onPageChange,
}) {
  const pageNumbers = useMemo(() => {
    const start = Math.max(0, Math.min(page - 2, totalPages - 5));
    return Array.from(
      { length: Math.min(5, totalPages) },
      (_, index) => start + index,
    );
  }, [page, totalPages]);

  // Clear all filters
  const clearFilters = () => {
    onSearchChange("");
    onTitleChange("");
    onSortChange("name");
  };

  const hasActiveFilters = searchTerm || filterTitle || sortBy !== "name";

  return (
    <div className="space-y-6">
      {/* Filters and Search Bar */}
      <div className="py-2 transition-colors duration-300">
        <div className="flex flex-col sm:flex-row gap-4">
          {/* Search */}
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-[#3D5A80]" />
            <input
              type="text"
              placeholder="Search contacts..."
              value={searchTerm}
              onChange={(e) => onSearchChange(e.target.value)}
              className={`w-full pl-10 pr-4 py-3 rounded-xl border-0 shadow-sm focus:outline-none focus:ring-2 focus:ring-[#98C1D9] transition-all duration-200 ${isDarkMode ? "bg-[#242B31] text-[#F7FAFC] placeholder:text-[#89939C]" : "bg-white text-[#293241] placeholder:text-[#8293A3]"}`}
            />
          </div>

          {/* Sort */}
          <div className="flex items-center gap-2">
            <ArrowUpDown className="h-5 w-5 text-[#3D5A80]" />
            <select
              value={sortBy}
              onChange={(e) => onSortChange(e.target.value)}
              className={`px-3 py-3 rounded-xl border-0 shadow-sm focus:outline-none focus:ring-2 focus:ring-[#98C1D9] transition-all duration-200 ${isDarkMode ? "bg-[#242B31] text-[#F7FAFC]" : "bg-white text-[#293241]"}`}
            >
              <option value="name">Sort by Name</option>
              <option value="email">Sort by Email</option>
              <option value="title">Sort by Title</option>
            </select>
          </div>

          {/* Title Filter */}
          {titles.length > 0 && (
            <div className="flex items-center gap-2">
              <Filter className="h-5 w-5 text-[#3D5A80]" />
              <select
                value={filterTitle}
                onChange={(e) => onTitleChange(e.target.value)}
                className={`px-3 py-3 rounded-xl border-0 shadow-sm focus:outline-none focus:ring-2 focus:ring-[#98C1D9] transition-all duration-200 ${isDarkMode ? "bg-[#242B31] text-[#F7FAFC]" : "bg-white text-[#293241]"}`}
              >
                <option value="">All Titles</option>
                {titles.map((title) => (
                  <option key={title} value={title}>
                    {title}
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Clear Filters */}
          {hasActiveFilters && (
            <button
              onClick={clearFilters}
              className={`px-4 py-2.5 text-sm rounded-full transition-all duration-200 flex items-center gap-1 ${isDarkMode ? "text-[#AFCBDD] hover:bg-white/10" : "text-[#16425B] hover:bg-[#E7F1F6]"}`}
            >
              <X className="h-4 w-4" />
              Clear
            </button>
          )}
        </div>
        {titlesError && (
          <div
            className={`mt-4 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-[#EE6C4D]/60 p-3 text-sm ${isDarkMode ? "bg-[#242B31] text-[#F7FAFC]" : "bg-white text-[#293241]"}`}
            role="alert"
          >
            <span>Unable to load title filters.</span>
            <button
              type="button"
              onClick={() => void onRetryTitles()}
              className="rounded-lg bg-[#16425B] px-3 py-2 font-semibold text-white hover:bg-[#3D5A80]"
             >
              Try Again
            </button>
          </div>
        )}
      </div>

      {/* Results Count */}
      <div className={`flex items-center justify-between text-sm ${isDarkMode ? "text-[#98C1D9]" : "text-[#3D5A80]"}`}>
        <p>
          Showing {contacts.length} of {totalElements} contacts
        </p>
      </div>

      {/* Contact Cards Grid */}
      {contacts.length === 0 ? (
        <div className={`text-center py-14 rounded-2xl ${isDarkMode ? "bg-[#242B31]" : "bg-white shadow-sm"}`}>
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-[#E0FBFC] mb-4">
            <Search className="h-7 w-7 text-[#3D5A80]" />
          </div>
          <h3 className={`text-lg font-medium ${isDarkMode ? "text-[#F7FAFC]" : "text-[#293241]"}`}>
            No contacts found
          </h3>
          <p className={`mt-1 ${isDarkMode ? "text-[#B7C0C7]" : "text-[#3D5A80]"}`}>
            {hasActiveFilters
              ? "Try adjusting your search or filters"
              : "Add your first contact to get started"}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {contacts.map((contact) => (
            <ContactCard
              key={contact.id}
            contact={contact}
            onView={onView}
              onEdit={onEdit}
              onDelete={onDelete}
              isDarkMode={isDarkMode}
            />
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <nav
          className="flex flex-wrap items-center justify-center gap-2 pt-2"
          aria-label="Contact pagination"
        >
          <button
            type="button"
            onClick={() => onPageChange(page - 1)}
            disabled={page === 0}
            className="rounded-lg bg-[#16425B] px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-[#3D5A80] disabled:cursor-not-allowed disabled:opacity-40"
          >
            Previous
          </button>
          {pageNumbers.map((pageNumber) => (
            <button
              type="button"
              key={pageNumber}
              onClick={() => onPageChange(pageNumber)}
              aria-current={pageNumber === page ? "page" : undefined}
              className={`h-9 min-w-9 rounded-lg px-2 text-sm font-semibold transition-colors ${
                pageNumber === page
                  ? "bg-[#EE6C4D] text-white"
                  : isDarkMode
                    ? "bg-[#242B31] text-[#E0FBFC] hover:bg-[#3D5A80]"
                    : "bg-white text-[#16425B] hover:bg-[#D9EAF2]"
              }`}
            >
              {pageNumber + 1}
            </button>
          ))}
          <button
            type="button"
            onClick={() => onPageChange(page + 1)}
            disabled={page >= totalPages - 1}
            className="rounded-lg bg-[#16425B] px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-[#3D5A80] disabled:cursor-not-allowed disabled:opacity-40"
          >
            Next
          </button>
          <span className={`w-full text-center text-sm ${isDarkMode ? "text-[#98C1D9]" : "text-[#3D5A80]"}`}>
            Page {page + 1} of {totalPages}
          </span>
        </nav>
      )}
    </div>
  );
}

export default ContactList;
