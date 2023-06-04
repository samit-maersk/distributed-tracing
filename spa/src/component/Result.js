import React, { useState } from 'react'

const Result = ({data, index}) => {
    const [show, setShow] = useState(false);
    const handleShow = () => setShow(show ? false : true);
    return (
        <>
            <tr>
                <td>Apple Inc.</td>
                <td>AAPL</td>       
                <td>
                    {show ? 
                        <i className="bi bi-chevron-up btn" type="button" data-bs-toggle="collapse" data-bs-target={"#collapseExample-"+index} aria-expanded="false" aria-controls={"collapseExample-"+index} onClick={handleShow}></i> :
                        <i className="bi bi-chevron-down btn" type="button" data-bs-toggle="collapse" data-bs-target={"#collapseExample-"+index} aria-expanded="false" aria-controls={"collapseExample-"+index} onClick={handleShow}></i>
                    }
                </td>
            </tr>
            <tr className="collapse" id={"collapseExample-"+index}>
                <td colSpan="12"> {data} </td>
            </tr>
        </>    
    )
}

export default Result