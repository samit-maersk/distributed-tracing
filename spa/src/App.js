import logo from './logo.svg';
import './App.css';
import Header from './component/Header';
import Dashboard from './component/Dashboard';
import Footer from './component/Footer';

function App() {
  return (
    <div className='container'>
      <header>
        <Header />
      </header>
      <article className='m-5'>
        <Dashboard />
      </article>
      <footer className='m-5'>
        <Footer />
      </footer>
      
    </div>
  );
}

export default App;
