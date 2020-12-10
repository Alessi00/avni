import React, { useEffect } from "react";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Grid from "@material-ui/core/Grid";
import { makeStyles } from "@material-ui/core/styles";
import Typography from "@material-ui/core/Typography";
import { useTranslation } from "react-i18next";
import DialogContent from "@material-ui/core/DialogContent";
import FormControl from "@material-ui/core/FormControl";
import InputLabel from "@material-ui/core/InputLabel";
import Modal from "./CommonModal";
import { getPrograms } from "../../../reducers/programReducer";
import { withRouter } from "react-router-dom";
import { connect } from "react-redux";
import { withParams } from "common/components/utils";
import NativeSelect from "@material-ui/core/NativeSelect";
import AccountCircle from "@material-ui/icons/AccountCircle";
import CustomizedBackdrop from "../../../components/CustomizedBackdrop";

const useStyles = makeStyles(theme => ({
  tableCellDetails: {
    borderBottom: "none",
    padding: "0px 21px 0px 11px",
    fontWeight: "500",
    color: "#1010101",
    fontSize: "14px"
  },
  enrollButtonStyle: {
    backgroundColor: "#f27510",
    height: "38px",
    zIndex: 1,
    boxShadow: "none",
    whiteSpace: "nowrap"
  },
  bigAvatar: {
    width: 42,
    height: 42,
    marginTop: "20px",
    marginBottom: "8px"
  },
  tableContainer: {
    marginTop: "10px"
  },
  tableView: {
    flexGrow: 1,
    alignItems: "center",
    justifyContent: "center"
  },
  mainHeading: {
    fontSize: "20px",
    fontWeight: "500"
  },
  tableCell: {
    color: "#555555",
    fontSize: "12px",
    borderBottom: "none",
    padding: "0px 0px 0px 11px",
    fontWeight: "500"
  },
  btnCustom: {
    float: "left",
    backgroundColor: "#fc9153",
    height: "30px"
  },
  cancelBtnCustom: {
    float: "left",
    backgroundColor: "#F8F9F9",
    color: "#fc9153",
    border: "1px solid #fc9153",
    height: "30px"
  },
  form: {
    display: "flex",
    flexDirection: "column",
    margin: "auto",
    minWidth: "450px",
    minHeight: "170px"
  },
  formControl: {
    marginTop: theme.spacing(2),
    minWidth: 120,
    width: "211px"
  },
  formControlLabel: {
    marginTop: theme.spacing(1)
  },
  selectEmpty: {
    width: "211px"
  },
  btnBottom: {
    margin: 0,
    padding: "11px",
    backgroundColor: "#F8F9F9",
    float: "left",
    display: "inline"
  },
  error: {
    color: "red",
    padding: "3px",
    fontSize: "12px"
  },
  errorText: {
    color: "red"
  },
  iconStyle: {
    fontSize: "50px",
    color: "#676173",
    marginTop: "10px"
  }
}));

const ProfileDetails = ({
  profileDetails,
  getPrograms,
  programs,
  subjectUuid,
  match,
  enableReadOnly,
  load
}) => {
  const classes = useStyles();
  const [selectedProgram, setSelectedProgram] = React.useState("");
  const [errorStatus, setError] = React.useState(false);

  const handleChange = event => {
    setSelectedProgram(event.target.value);
    setError(!event.target.value);
  };

  const handleError = isError => {
    setError(isError);
  };

  const { t } = useTranslation();

  useEffect(() => {
    getPrograms(subjectUuid);
  }, []);
  const close = () => {
    return true;
  };

  const content = (
    <DialogContent>
      <form className={classes.form} noValidate>
        <FormControl className={classes.formControl}>
          <InputLabel
            shrink
            id="demo-simple-select-placeholder-label-label"
            className={errorStatus ? classes.errorText : ""}
          >
            Program
          </InputLabel>

          <NativeSelect
            value={selectedProgram}
            onChange={handleChange}
            inputProps={{
              name: "selected_program",
              id: "selected_program-native-helper"
            }}
            error={errorStatus}
          >
            <option key={"emptyElement"} value="" />

            {programs
              ? programs.map((element, index) => (
                  <option key={index} value={element.name}>
                    {element.name}
                  </option>
                ))
              : ""}
          </NativeSelect>
          {errorStatus ? <div className={classes.error}>Please select program to enrol.</div> : ""}
        </FormControl>
      </form>
    </DialogContent>
  );

  return (
    <div className={classes.tableView}>
      <CustomizedBackdrop load={load} />
      <Typography component={"span"} className={classes.mainHeading}>
        {`${profileDetails.nameString}`}
      </Typography>
      <Grid alignItems="center" container spacing={1}>
        {/* <Grid item>
          <Avatar
            src="https://cdn2.iconfinder.com/data/icons/circle-icons-1/64/profle-512.png"
            className={classes.bigAvatar}
          />

        </Grid> */}
        <Grid item>
          <AccountCircle className={classes.iconStyle} />
        </Grid>
        <Grid item xs={4}>
          <Table aria-label="caption table" className={classes.tableContainer}>
            <TableHead>
              <TableRow className={classes.tableHeader}>
                {profileDetails.subjectType.isPerson() && (
                  <TableCell className={classes.tableCell}>{t("gender")}</TableCell>
                )}
                {profileDetails.subjectType.isPerson() && (
                  <TableCell className={classes.tableCell}>{t("Age")}</TableCell>
                )}
                <TableCell className={classes.tableCell}>
                  {t(profileDetails.lowestAddressLevel.type)}
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                {profileDetails.subjectType.isPerson() && (
                  <TableCell className={classes.tableCellDetails}>
                    {t(profileDetails.gender.name)}
                  </TableCell>
                )}
                {profileDetails.subjectType.isPerson() && (
                  <TableCell className={classes.tableCellDetails}>
                    {profileDetails.dateOfBirth
                      ? new Date().getFullYear() -
                        new Date(profileDetails.dateOfBirth).getFullYear() +
                        " " +
                        `${t("years")}`
                      : "-"}
                  </TableCell>
                )}
                <TableCell className={classes.tableCellDetails}>
                  {profileDetails.lowestAddressLevel.name}
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </Grid>
        <Grid item xs={7} align="right">
          {!enableReadOnly && !profileDetails.voided ? (
            <div>
              <Modal
                content={content}
                handleError={handleError}
                buttonsSet={[
                  {
                    buttonType: "openButton",
                    label: t("enrolInProgram"),
                    classes: classes.enrollButtonStyle
                  },
                  {
                    buttonType: "saveButton",
                    label: t("Enrol"),
                    classes: classes.btnCustom,
                    redirectTo: `/app/subject/enrol?uuid=${subjectUuid}&programName=${selectedProgram}&formType=ProgramEnrolment&subjectTypeName=${
                      profileDetails.subjectType.name
                    }`,
                    requiredField: selectedProgram,
                    handleError: handleError
                  },
                  {
                    buttonType: "cancelButton",
                    label: t("Cancel"),
                    classes: classes.cancelBtnCustom
                  }
                ]}
                title={t("Enrol in program")}
                btnHandleClose={close}
              />
            </div>
          ) : (
            ""
          )}
        </Grid>
      </Grid>
    </div>
  );
};

const mapStateToProps = state => ({
  programs: state.dataEntry.programs ? state.dataEntry.programs.programs : "",
  load: state.dataEntry.loadReducer.load
});

const mapDispatchToProps = {
  getPrograms
};

export default withRouter(
  withParams(
    connect(
      mapStateToProps,
      mapDispatchToProps
    )(ProfileDetails)
  )
);
