import { all, call, fork, put, takeLatest } from "redux-saga/effects";
import { types, setCompletedVisit } from "../reducers/completedVisitReducer";
import { mapViewVisit } from "../../common/subjectModelMapper";

import api from "../api";

export default function*() {
  yield all([completedVisitFetchWatcher].map(fork));
}

export function* completedVisitFetchWatcher() {
  yield takeLatest(types.GET_COMPLETEDVISIT, completedVisitFetchWorker);
}

export function* completedVisitFetchWorker({ completedVisitUuid }) {
  const completedVisit = yield call(api.fetchcompletedVisit, completedVisitUuid);
  yield put(setCompletedVisit(mapCompletedVisit(completedVisit)));
}
