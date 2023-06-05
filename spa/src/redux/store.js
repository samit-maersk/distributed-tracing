import { configureStore } from '@reduxjs/toolkit'
import clientReducer, {fetchAll } from './clientSlice'
export const store = configureStore({
  reducer: {
    employee: clientReducer,
  },
})
store.dispatch(fetchAll());