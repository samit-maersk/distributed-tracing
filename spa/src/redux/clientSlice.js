import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import axios from 'axios'

export const fetchAll = createAsyncThunk(
    'employee/fetchall', 
    async () => {
        const response = await axios.get('http://localhost:8080/employee')
        return response.data
})

const initialState = {
  data: [],
  error: null,
  loading: false
}

export const clientSlice = createSlice({
  name: 'client',
  initialState,
  reducers: {
  },
  extraReducers: (builder) => {
    builder.addCase(fetchAll.pending, (state, action) => {
        state.loading = true
        }
    )
    builder.addCase(fetchAll.fulfilled, (state, action) => {
        state.loading = false
        state.data = action.payload
        }
    )
    builder.addCase(fetchAll.rejected, (state, action) => {
        state.loading = false
        state.error = action.error.message
        }
    )
  },
})

// Action creators are generated for each case reducer function
//export const {} = clientSlice.actions

export default clientSlice.reducer