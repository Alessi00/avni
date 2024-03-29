import React from "react";
import { Condition } from "rules-config";
import Grid from "@material-ui/core/Grid";
import Chip from "@material-ui/core/Chip";
import { toUpper } from "lodash";

const ConditionConjunctionComponent = ({ onConjunctionChange, value, ...props }) => {
  const chipColor = chipConjunction => (value === chipConjunction ? "primary" : "default");
  const andConjunction = Condition.conjunctions.And;
  const orConjunction = Condition.conjunctions.Or;

  return (
    <Grid container direction={"row"} justify={"center"}>
      <Grid item>
        <Chip
          style={{ marginTop: "15px", marginBottom: "15px" }}
          label={toUpper(andConjunction)}
          color={chipColor(andConjunction)}
          onClick={() => onConjunctionChange(andConjunction)}
        />
      </Grid>
      <Grid item>
        <Chip
          style={{ marginTop: "15px", marginBottom: "15px" }}
          label={toUpper(orConjunction)}
          color={chipColor(orConjunction)}
          onClick={() => onConjunctionChange(orConjunction)}
        />
      </Grid>
    </Grid>
  );
};

export default ConditionConjunctionComponent;
