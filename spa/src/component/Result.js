import React, { useState } from 'react'

const Result = ({data, index}) => {
    
    const [show, setShow] = useState(false);
    const handleShow = () => setShow(show ? false : true);
    return (
        <>
            <tr>
                <td>{data.id}</td>
                <td>{data.name}</td>
                <td>{data.email}</td>
                <td>-</td>
                <td>{data.phone}</td>
                <td>{data.website}</td>
                <td>-</td>
                <td>-</td>
                <td>
                    {show ? 
                        <i className="bi bi-chevron-up btn" type="button" data-bs-toggle="collapse" data-bs-target={"#collapseExample-"+index} aria-expanded="false" aria-controls={"collapseExample-"+index} onClick={handleShow}></i> :
                        <i className="bi bi-chevron-down btn" type="button" data-bs-toggle="collapse" data-bs-target={"#collapseExample-"+index} aria-expanded="false" aria-controls={"collapseExample-"+index} onClick={handleShow}></i>
                    }
                </td>
            </tr>
            <tr className="collapse" id={"collapseExample-"+index}>
                <td colSpan="12">
                    <div className="card-group">
                            <div className="card">
                                <div className="card-body">
                                    <h5 className="card-title">Address</h5>
                                    <p className="card-text">{data.address.street}</p>
                                    <p className="card-text">{data.address.suite}</p>
                                    <p className="card-text">{data.address.city}</p>
                                    <p className="card-text">{data.address.zipcode}</p>
                                </div>
                            </div>
                            <div className="card">
                                <div className="card-body">
                                    <h5 className="card-title">Geo</h5>
                                    <p className="card-text">{data.address.geo.lat}</p>
                                    <p className="card-text">{data.address.geo.lng}</p>
                                </div>
                            </div>
                            <div className="card">
                                <div className="card-body">
                                    <h5 className="card-title">Company</h5>
                                    <p className="card-text">{data.company.name}</p>
                                    <p className="card-text">{data.company.catchPhrase}</p>
                                    <p className="card-text">{data.company.bs}</p>
                                </div>
                            </div>
                            <div className="card">
                                <div className="card-body">
                                    <h5 className="card-title">Employment</h5>
                                    <p className="card-text">{data.employment.id}</p>
                                    <p className="card-text">{data.employment.designation}</p>
                                    <p className="card-text">{data.employment.salary}</p>
                                </div>
                            </div>
                    </div>
                </td>
            </tr>
        </>    
    )
}

export default Result