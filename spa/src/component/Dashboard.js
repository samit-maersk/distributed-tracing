import React from 'react'
import Results from './Results'

const Dashboard = () => {

    return (
        <>
            <div className="d-flex justify-content-center">
                <div className="form-floating">
                    <input className="form-control" placeholder="Search" id="searchBox"></input>
                    <label htmlFor="searchBox">
                        <i className="bi bi-search"></i> Search
                    </label>
                </div>
                
                <button className="btn btn-info ms-2">
                    <i className="bi bi-arrow-return-left"></i>
                </button>

                <button className="btn btn-danger ms-2">
                <i class="bi bi-x-circle"></i>
                </button>
                
            </div>
            <div className='row m-5'>
                <Results />
            </div>
        </>
        
    )
}

export default Dashboard