import React, { useEffect } from "react";
import { Route, withRouter } from "react-router-dom";
import { connect, useDispatch, useSelector } from "react-redux";
import SubjectSearch from "./views/search/SubjectSearch";
import SubjectRegister from "./views/registration/SubjectRegister";
import {
  getOperationalModules,
  getLegacyRulesBundle,
  selectLegacyRulesBundleLoaded,
  selectLegacyRulesLoaded,
  getLegacyRules
} from "dataEntryApp/reducers/metadataReducer";
import { getOrgConfigInfo } from "i18nTranslations/TranslationReducers";
import Loading from "./components/Loading";
import DataEntryDashboard from "./views/dashboardNew/dashboardNew";
import SubjectDashboard from "./views/subjectDashBoard/SubjectDashboard";
import ProgramEnrol from "./views/subjectDashBoard/components/ProgramEnrol";
import ViewVisit from "./views/subjectDashBoard/components/ViewVisit";
import AddRelative from "./views/subjectDashBoard/components/AddRelative";
import CompletedVisits from "./views/subjectDashBoard/components/CompletedVisits";
import NewProgramVisit from "./views/subjectDashBoard/components/NewProgramVisit";
import ProgramEncounter from "./views/subjectDashBoard/components/ProgramEncounter";
import CancelProgramEncounter from "./views/subjectDashBoard/components/CancelProgramEncounter";
import NewGeneralVisit from "./views/subjectDashBoard/components/NewGeneralVisit";
import SearchFilterFormContainer from "./views/GlobalSearch/SearchFilterForm";
import Encounter from "./views/subjectDashBoard/components/Encounter";
import CancelEncounter from "./views/subjectDashBoard/components/CancelEncounter";
import AppBar from "dataEntryApp/components/AppBar";
import Grid from "@material-ui/core/Grid";
import { makeStyles } from "@material-ui/core/styles";
import i18n from "i18next";
import { I18nextProvider } from "react-i18next";

const useStyles = makeStyles(theme => ({
  root: {
    flexGrow: 1
  }
}));

const DataEntry = ({ match: { path }, operationalModules, orgConfig }) => {
  const classes = useStyles();
  const dispatch = useDispatch();
  const legacyRulesBundleLoaded = useSelector(selectLegacyRulesBundleLoaded);
  const legacyRulesLoaded = useSelector(selectLegacyRulesLoaded);

  useEffect(() => {
    dispatch(getOperationalModules());
    dispatch(getOrgConfigInfo());
    dispatch(getLegacyRulesBundle());
    dispatch(getLegacyRules());
  }, []);

  return operationalModules && orgConfig && legacyRulesBundleLoaded && legacyRulesLoaded ? (
    <I18nextProvider i18n={i18n}>
      <div className={classes.root}>
        {/* <Grid container spacing={2} justify="center"> */}
        <Grid container justify="center">
          <Grid item xs={12}>
            <AppBar />
          </Grid>
          <Grid item xs={12}>
            <Route path={[path, `${path}/dashboard`]} component={DataEntryDashboard} />
            <Route exact path={[path, `${path}/search`]} component={SubjectSearch} />
            <Route path={`${path}/register`} component={SubjectRegister} />
            <Route path={`${path}/editSubject`} component={SubjectRegister} />
            <Route
              exact
              path={`${path}/subject`}
              component={SubjectDashboard}
              key={`${Math.random()}`}
            />
            <Route
              exact
              path={`${path}/subject/subjectProfile`}
              component={(...props) => <SubjectDashboard tab={1} {...props} />}
            />
            {/* <Route exact path={`${path}/subject`} component={SubjectDashboard} /> */}
            <Route exact path={`${path}/subject/enrol`} component={ProgramEnrol} />
            <Route exact path={`${path}/subject/viewProgramEncounter`} component={ViewVisit} />
            <Route exact path={`${path}/subject/viewEncounter`} component={ViewVisit} />
            <Route exact path={`${path}/subject/addRelative`} component={AddRelative} />
            {/* <Route exact path={`${path}/completeVisit/:id/:uuid`} component={CompleteVisit} /> */}
            <Route exact path={`${path}/subject/completedEncounters`} component={CompletedVisits} />
            <Route
              exact
              path={`${path}/subject/completedProgramEncounters`}
              component={CompletedVisits}
            />
            <Route exact path={`${path}/subject/newProgramVisit`} component={NewProgramVisit} />
            <Route exact path={`${path}/subject/programEncounter`} component={ProgramEncounter} />
            <Route path={`${path}/subject/editProgramEncounter`} component={ProgramEncounter} />
            <Route
              path={`${path}/subject/cancelProgramEncounter`}
              component={CancelProgramEncounter}
            />
            <Route
              path={`${path}/subject/editCancelProgramEncounter`}
              component={CancelProgramEncounter}
            />
            <Route exact path={`${path}/subject/newGeneralVisit`} component={NewGeneralVisit} />
            <Route exact path={`${path}/searchFilter`} component={SearchFilterFormContainer} />
            <Route exact path={`${path}/subject/encounter`} component={Encounter} />
            <Route path={`${path}/subject/editEncounter`} component={Encounter} />
            <Route path={`${path}/subject/cancelEncounter`} component={CancelEncounter} />
            <Route path={`${path}/subject/editCancelEncounter`} component={CancelEncounter} />
          </Grid>
        </Grid>
      </div>
    </I18nextProvider>
  ) : (
    <Loading />
  );
};

const mapStateToProps = state => ({
  operationalModules: state.dataEntry.metadata.operationalModules,
  orgConfig: state.translationsReducer.orgConfig
});

export default withRouter(
  connect(
    mapStateToProps,
    null
  )(DataEntry)
);
