import { useState, useEffect } from "react";
import { LoginPage } from "./components/LoginPage";
import { RegisterPage } from "./components/RegisterPage";
import { Dashboard } from "./components/Dashboard";

interface User {
  name: string;
  email: string;
}

type Page = "login" | "register" | "dashboard";

export default function App() {
  const [currentPage, setCurrentPage] = useState<Page>("login");
  const [user, setUser] = useState<User | null>(null);

  // Load user from localStorage on app start
  useEffect(() => {
    const savedUser = localStorage.getItem("subscriptions_user");
    if (savedUser) {
      setUser(JSON.parse(savedUser));
      setCurrentPage("dashboard");
    }
  }, []);

  const handleLogin = (email: string, password: string) => {
    // В реальном приложении здесь будет вызов API
    // Сейчас токен сохраняется в LoginPage
    const mockUser: User = {
      name: email.split("@")[0],
      email: email,
    };
    
    setUser(mockUser);
    localStorage.setItem("subscriptions_user", JSON.stringify(mockUser));
    setCurrentPage("dashboard");
  };

  const handleRegister = (email: string, password: string, name: string) => {
    // В реальном приложении здесь будет вызов API
    // Сейчас токен сохраняется в RegisterPage
    const newUser: User = {
      name: name,
      email: email,
    };
    
    setUser(newUser);
    localStorage.setItem("subscriptions_user", JSON.stringify(newUser));
    setCurrentPage("dashboard");
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem("subscriptions_user");
    setCurrentPage("login");
  };

  const switchToRegister = () => {
    setCurrentPage("register");
  };

  const switchToLogin = () => {
    setCurrentPage("login");
  };

  if (currentPage === "login") {
    return (
      <LoginPage 
        onLogin={handleLogin} 
        onSwitchToRegister={switchToRegister} 
      />
    );
  }

  if (currentPage === "register") {
    return (
      <RegisterPage 
        onRegister={handleRegister} 
        onSwitchToLogin={switchToLogin} 
      />
    );
  }

  if (currentPage === "dashboard" && user) {
    return (
      <Dashboard 
        user={user} 
        onLogout={handleLogout} 
      />
    );
  }

  // Fallback
  return (
    <LoginPage 
      onLogin={handleLogin} 
      onSwitchToRegister={switchToRegister} 
    />
  );
}