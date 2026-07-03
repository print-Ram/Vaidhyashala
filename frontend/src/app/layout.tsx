import type { Metadata } from "next";
import { Geist, Geist_Mono, Inter, Cinzel } from "next/font/google";
import "./globals.css";
import { cn } from "@/lib/utils";
import { GoogleOAuthProvider } from "@react-oauth/google";

const inter = Inter({subsets:['latin'],variable:'--font-sans'});

const cinzel = Cinzel({
  subsets: ["latin"],
  variable: "--font-brand",
  weight: ["700", "800"],
});

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Vaidhyashala",
  description: "Authentic Care, Simplified",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const googleClientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID ?? "";

  return (
    <html
      lang="en"
      className={cn(
        "h-full", 
        "antialiased", 
        geistSans.variable, 
        geistMono.variable, 
        "font-sans", 
        inter.variable,
        cinzel.variable
      )}
    >
      <body className="min-h-full flex flex-col bg-slate-50 text-slate-800">
        <GoogleOAuthProvider clientId={googleClientId}>
          {children}
        </GoogleOAuthProvider>
      </body>
    </html>
  );
}



