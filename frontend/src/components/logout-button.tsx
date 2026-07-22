import React from "react";
import { Button } from "./ui/button";

type TLogout = React.PropsWithChildren<React.ComponentProps<"button">>;

export default function LogoutButton({ children, ...props }: TLogout) {
  return (
    <Button variant={"destructive"} {...props}>
      {children}
    </Button>
  );
}
