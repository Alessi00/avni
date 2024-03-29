import React from "react";
import Fab from "@material-ui/core/Fab";
import { makeStyles } from "@material-ui/core/styles";

const useStyles = makeStyles(theme => ({
  ButtonStyle: {
    marginBottom: theme.spacing(2),
    marginRight: "10px",
    height: "28px"
  }
}));

const Button = ({ btnLabel, btnClass, btnClick, btnDisabled, id, ...props }) => {
  const classes = useStyles();
  return (
    <Fab
      className={btnClass ? (classes.ButtonStyle, btnClass) : classes.ButtonStyle}
      variant="extended"
      color="primary"
      aria-label="add"
      onClick={btnClick}
      disabled={btnDisabled}
      id={id}
      {...props}
    >
      {btnLabel}
    </Fab>
  );
};

export default Button;
