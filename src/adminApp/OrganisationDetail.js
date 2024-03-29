import React from "react";
import { DocumentationContainer } from "../common/components/DocumentationContainer";
import Box from "@material-ui/core/Box";
import Grid from "@material-ui/core/Grid";
import { Title } from "react-admin";
import Button from "@material-ui/core/Button";
import { DeleteData } from "./components/DeleteData";
import { makeStyles } from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import { OrgSettings } from "./components/OrgSettings";

const useStyles = makeStyles(theme => ({
  deleteButton: {
    backgroundColor: "red"
  }
}));

export const OrganisationDetail = ({ organisation: { name }, hasEditPrivilege }) => {
  const classes = useStyles();
  const [openModal, setOpenModal] = React.useState(false);

  return (
    <Box boxShadow={2} p={3} bgcolor="background.paper">
      <DocumentationContainer filename={"OrganisationDetail.md"}>
        <Title title={"Organisation Details"} />
        <Grid container direction={"row"} spacing={10}>
          <Grid item container spacing={3}>
            <Grid item>
              <Typography variant="h6" gutterBottom>
                Organisation Name : {name}
              </Typography>
            </Grid>
            {hasEditPrivilege && (
              <Grid item>
                <Button
                  className={classes.deleteButton}
                  variant="contained"
                  color="secondary"
                  onClick={() => setOpenModal(true)}
                >
                  Delete all data
                </Button>
              </Grid>
            )}
          </Grid>
          <OrgSettings hasEditPrivilege={hasEditPrivilege} />
        </Grid>
        {hasEditPrivilege && (
          <DeleteData openModal={openModal} setOpenModal={setOpenModal} orgName={name} />
        )}
      </DocumentationContainer>
    </Box>
  );
};
