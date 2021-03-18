import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import { AvniSwitch } from "../../common/components/AvniSwitch";
import React from "react";
import http from "common/utils/httpClient";

export const OrgSettings = () => {
  const [orgSettings, setOrgSettings] = React.useState();

  React.useEffect(() => {
    http
      .fetchJson("/web/organisationConfig")
      .then(response => response.json)
      .then(({ organisationConfig }) => setOrgSettings(organisationConfig));
  }, []);

  const onSettingsChange = (settingsName, value) => {
    const payload = { settings: { [settingsName]: value } };
    http
      .put("/organisationConfig", payload)
      .then(response => {
        if (response.status === 200 || response.status === 201) {
          setOrgSettings(response.data.settings);
        }
      })
      .catch(error => console.error(error));
  };

  const organisationConfigSettingKeys = {
    approvalWorkflow: "enableApprovalWorkflow",
    draftSave: "saveDrafts",
    hideDateOfBirth: "hideDateOfBirth"
  };

  return orgSettings ? (
    <Grid item container direction={"column"}>
      <Grid item>
        <Typography variant="h5" gutterBottom>
          Organisation settings
        </Typography>
      </Grid>
      <Grid item container spacing={1} direction={"column"}>
        <Grid item>
          <AvniSwitch
            switchFirst
            checked={orgSettings[organisationConfigSettingKeys.approvalWorkflow]}
            onChange={event =>
              onSettingsChange(organisationConfigSettingKeys.approvalWorkflow, event.target.checked)
            }
            name="Approval workflow"
            toolTipKey={"ADMIN_APPROVAL_WORKFLOW"}
          />
        </Grid>
        {orgSettings[organisationConfigSettingKeys.approvalWorkflow] && (
          <Grid item>
            <Typography variant="body2" gutterBottom>
              Create custom dashboard with standard card types approve, reject and pending to track
              the approval workflow.
            </Typography>
          </Grid>
        )}
        <Grid item>
          <AvniSwitch
            switchFirst
            checked={orgSettings[organisationConfigSettingKeys.draftSave]}
            onChange={event =>
              onSettingsChange(organisationConfigSettingKeys.draftSave, event.target.checked)
            }
            name="Draft save"
            toolTipKey={"ADMIN_SAVE_DRAFT"}
          />
        </Grid>
        <Grid item>
          <AvniSwitch
            switchFirst
            checked={orgSettings[organisationConfigSettingKeys.hideDateOfBirth]}
            onChange={event =>
              onSettingsChange(organisationConfigSettingKeys.hideDateOfBirth, event.target.checked)
            }
            name="Hide Date of Birth on DEA"
            toolTipKey={"ADMIN_HIDE_DOB"}
          />
        </Grid>
      </Grid>
    </Grid>
  ) : (
    <div />
  );
};
