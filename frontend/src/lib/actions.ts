"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { LOGOUT_ENDPOINT } from "./secrets";

export async function signOut() {
  const cookieStore = await cookies(); // await required in Next.js 15+
  const accessToken = cookieStore.get("accessToken")?.value;

  try {
    await fetch(LOGOUT_ENDPOINT, {
      method: "POST",
    });
  } catch (error) {
    console.error("Logout API call failed:", error);
  }
  //   cookieStore.delete("accessToken");

  //   redirect("/login");
}
