import { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import ProjectCard from '../components/ProjectCard';
import apiClient from '../api/apiClient';

export default function Dashboard() {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [creating, setCreating] = useState(false);
  const [formError, setFormError] = useState('');

  useEffect(() => {
    loadProjects();
  }, []);

  async function loadProjects() {
    setLoading(true);
    setError('');
    try {
      const data = await apiClient('/projects');
      setProjects(data);
    } catch (err) {
      setError(err.message || 'Could not load projects');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(e) {
    e.preventDefault();
    setFormError('');
    setCreating(true);
    try {
      const project = await apiClient('/projects', {
        method: 'POST',
        body: JSON.stringify({ name, description }),
      });
      setProjects((prev) => [project, ...prev]);
      setName('');
      setDescription('');
      setShowForm(false);
    } catch (err) {
      setFormError(err.message || 'Could not create project');
    } finally {
      setCreating(false);
    }
  }

  return (
    <>
      <Navbar />
      <main className="page">
        <div className="container">
          <div className="page-header">
            <div>
              <h1>Your projects</h1>
              <p>Every project you own or have been added to.</p>
            </div>
            {!showForm && (
              <button className="btn btn-primary" onClick={() => setShowForm(true)}>
                + New project
              </button>
            )}
          </div>

          {showForm && (
            <div className="panel">
              <div className="panel-header">
                <h3>New project</h3>
                <button className="btn btn-ghost btn-sm" onClick={() => setShowForm(false)}>Cancel</button>
              </div>
              {formError && <div className="error-text">{formError}</div>}
              <form onSubmit={handleCreate}>
                <div className="field">
                  <label className="label" htmlFor="pname">Name</label>
                  <input
                    id="pname"
                    className="input"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                    autoFocus
                  />
                </div>
                <div className="field">
                  <label className="label" htmlFor="pdesc">Description</label>
                  <textarea
                    id="pdesc"
                    className="textarea"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="What is this project for?"
                  />
                </div>
                <button className="btn btn-primary" type="submit" disabled={creating}>
                  {creating ? 'Creating…' : 'Create project'}
                </button>
              </form>
            </div>
          )}

          {loading && <p className="spinner-text">Loading projects…</p>}
          {error && !loading && <div className="error-text">{error}</div>}

          {!loading && !error && projects.length === 0 && !showForm && (
            <div className="empty-state">
              <h3>No projects yet</h3>
              <p>Create your first project to start assigning tasks.</p>
            </div>
          )}

          {!loading && !error && projects.length > 0 && (
            <div className="project-grid">
              {projects.map((p) => (
                <ProjectCard key={p.id} project={p} />
              ))}
            </div>
          )}
        </div>
      </main>
    </>
  );
}
