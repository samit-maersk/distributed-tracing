import React from 'react'

const Results = () => {

    return (
        <table className="table table-striped">
            <thead class="table-light">
                <tr>
                    <th scope="col">Name</th>
                    <th scope="col">Symbol</th>
                    <th scope="col">Price</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>Apple Inc.</td>
                    <td>AAPL</td>
                    <td>?</td>
                </tr>
                <tr>
                    <td>Apple Inc.</td>
                    <td>AAPL</td>
                    <td>123.45</td>
                </tr>
            </tbody>
        </table>

    )
}

export default Results