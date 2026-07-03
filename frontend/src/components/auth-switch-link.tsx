import Link from "next/link";

type AuthSwitchLinkProps = {
  href: string;
  children: string;
};

export default function AuthSwitchLink({
  href,
  children,
}: AuthSwitchLinkProps) {
  return (
    <Link
      href={href}
      className="font-semibold inline-block text-(--blue-600) underline underline-offset-4 decoration-(--blue-200) hover:decoration-(--blue-600)"
    >
      {children}
    </Link>
  );
}
