import referenceDataSaga from "./referenceDataSaga";
import subjectSaga from "./subjectSaga";
import { all, fork } from "redux-saga/effects";
import subjectDashboardSaga from "./subjectDashboardSaga";
import generalSubjectDashboardSaga from "./generalSubjectDashboardSaga";
import programSubjectDashboardSaga from "./programSubjectDashboardSaga";

export default function* rootSaga() {
  yield all(
    [
      referenceDataSaga,
      subjectSaga,
      subjectDashboardSaga,
      generalSubjectDashboardSaga,
      programSubjectDashboardSaga
    ].map(fork)
  );
}