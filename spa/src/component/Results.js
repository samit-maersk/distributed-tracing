import React from 'react'
import Result from './Result'

const Results = () => {

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
                {[1,2,3].map((n,i) => <Result key={i} data={n} index={i}/>)}
            </tbody>
        </table>

    )
}

export default Results