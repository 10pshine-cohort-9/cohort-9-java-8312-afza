import { useCallback, useEffect, useState } from "react";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Profile from "./pages/Profile";
import Registration from "./pages/Registration";
import LoadingSpinner from "./components/LoadingSpinner";
import ErrorBoundary from "./components/ErrorBoundary";
import { getSession, logout as logoutRequest } from "./api/authApi";
import { setUnauthorizedHandler } from "./api/contactApi";

function App() {
  const [user, setUser] = useState(null);
  const [page, setPage] = useState("login");
  const [checkingSession, setCheckingSession] = useState(true);

  const clearAuthentication = useCallback(() => {
    setUser(null);
    setPage("login");
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(clearAuthentication);
    getSession()
      .then(({ data }) => {
        setUser(data);
        setPage("home");
      })
      .catch(() => clearAuthentication())
      .finally(() => setCheckingSession(false));
    return () => setUnauthorizedHandler(undefined);
  }, [clearAuthentication]);

  const handleLogout = async () => {
    try {
      await logoutRequest();
    } catch (error) {
      console.error("Logout request failed:", error);
    } finally {
      clearAuthentication();
    }
  };

  if (checkingSession) {
    return <main className="flex min-h-screen items-center justify-center bg-[#E0FBFC]"><LoadingSpinner /></main>;
  }

  return (
    <ErrorBoundary>
      {!user ? (
        page === "register" ? (
          <Registration onBack={() => setPage("login")} onRegistration={(profile) => { setUser(profile); setPage("home"); }} />
        ) : (
          <Login onLogin={(profile) => { setUser(profile); setPage("home"); }} onRegister={() => setPage("register")} />
        )
      ) : page === "profile" ? (
        <Profile user={user} onBack={() => setPage("home")} onLogout={handleLogout} />
      ) : (
        <Home onProfile={() => setPage("profile")} onUnauthorized={clearAuthentication} />
      )}
    </ErrorBoundary>
  );
}

export default App;
