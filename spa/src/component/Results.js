import React from 'react'
import Result from './Result'
import { useSelector } from 'react-redux'

const Results = () => {
    const {data, loading, error} = useSelector(state => state.employee)
    if (loading) {
        return (
            <div className="d-flex justify-content-center">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        )
    }

    if(error) {
        return (
            <h1>Error {error?.message}</h1>
        )
    }

    return (
        <table className="table">
            <thead className="table-light">
                <tr>
                    <th scope="col">Name</th>
                    <th scope="col">Symbol</th>
                    <th scope="col">Price</th>
                </tr>
            </thead>
            <tbody>
                {data.map((n,i) => <Result key={i} data={n} index={i}/>)}
            </tbody>
        </table>

    )
}

export default Results