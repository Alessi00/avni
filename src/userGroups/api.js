import http from "common/utils/httpClient";
import { get } from "lodash";

export default {
  fetchAllGroups: () => http.fetchJson("/web/groups").then(response => response.json),
  createGroups: name =>
    http
      .postJson("web/groups", { name })
      //returns [response, error]
      .then(r => [r.text, null])
      .catch(r => [null, `${get(r, "response.data") || get(r, "message") || "unknown error"}`]),
  updateGroup: (id, name, hasAllPrivileges) =>
    http
      .putJson("web/group", { id, name, hasAllPrivileges })
      .then(r => [r.data, null])
      .catch(r => [null, `${get(r, "response.data") || get(r, "message") || "unknown error"}`]),
  fetchAllUsers: () => http.fetchJson("/user/search/findAll").then(response => response.json),
  fetchGroupUsers: group_id =>
    http.fetchJson(`/groups/${group_id}/users`).then(response => response.json),
  fetchGroupPrivileges: group_id =>
    http.fetchJson(`/groups/${group_id}/privileges`).then(response => response.json),
  addUsersToGroup: users =>
    http
      .postJson("/userGroup", users)
      .then(r => [r.data, null])
      .catch(r => [null, `${get(r, "response.data") || get(r, "message") || "unknown error"}`]),
  removeUserFromGroup: id =>
    http
      .postJson("/userGroup/" + id)
      .then(r => [r.data, null])
      .catch(r => [null, `${get(r, "response.data") || get(r, "message") || "unknown error"}`]),
  //  fetchAllPrivileges: () => http.fetchJson("/web/groupPrivileges").then(response => response.json),
  modifyGroupPrivileges: params =>
    http
      .postJson("/groupPrivilege", params)
      .then(r => [r.data, null])
      .catch(r => [null, `${get(r, "response.data") || get(r, "message") || "unknown error"}`])
};
