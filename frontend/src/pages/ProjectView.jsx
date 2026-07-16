import { useEffect, useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import TaskCard from '../components/TaskCard';
import apiClient from '../api/apiClient';
import { useAuth } from '../context/AuthContext';

const COLUMNS = [
  { key: 'TODO', label: 'To do', dot: 'todo' },
  { key: 'IN_PROGRESS', label: 'In progress', dot: 'progress' },
  { key: 'DONE', label: 'Done', dot: 'done' },
];

export default function ProjectView() {
  const { id } = useParams();
  const { user } = useAuth();

  const [project, setProject] = useState(null);
  const [members, setMembers] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showMemberForm, setShowMemberForm] = useState(false);
  const [memberEmail, setMemberEmail] = useState('');
  const [memberRole, setMemberRole] = useState('MEMBER');
  const [memberError, setMemberError] = useState('');
  const [addingMember, setAddingMember] = useState(false);

  const [showTaskForm, setShowTaskForm] = useState(false);
  const [taskTitle, setTaskTitle] = useState('');
  const [taskDesc, setTaskDesc] = useState('');
  const [taskAssignee, setTaskAssignee] = useState('');
  const [taskDueDate, setTaskDueDate] = useState('');
  const [taskError, setTaskError] = useState('');
  const [creatingTask, setCreatingTask] = useState(false);

  const isAdmin = project?.currentUserRole === 'ADMIN';

  const loadAll = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [projectData, membersData, tasksData] = await Promise.all([
        apiClient(`/projects/${id}`),
        apiClient(`/projects/${id}/members`),
        apiClient(`/projects/${id}/tasks`),
      ]);
      setProject(projectData);
      setMembers(membersData);
      setTasks(tasksData);
      if (membersData.length > 0) setTaskAssignee(membersData[0].email);
    } catch (err) {
      setError(err.message || 'Could not load project');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadAll();
  }, [loadAll]);

  async function handleAddMember(e) {
    e.preventDefault();
    setMemberError('');
    setAddingMember(true);
    try {
      const newMember = await apiClient(`/projects/${id}/members`, {
        method: 'POST',
        body: JSON.stringify({ email: memberEmail, role: memberRole }),
      });
      setMembers((prev) => [...prev, newMember]);
      setProject((prev) => ({ ...prev, memberCount: prev.memberCount + 1 }));
      setMemberEmail('');
      setMemberRole('MEMBER');
      setShowMemberForm(false);
    } catch (err) {
      setMemberError(err.message || 'Could not add member');
    } finally {
      setAddingMember(false);
    }
  }

  async function handleCreateTask(e) {
    e.preventDefault();
    setTaskError('');
    setCreatingTask(true);
    try {
      const newTask = await apiClient(`/projects/${id}/tasks`, {
        method: 'POST',
        body: JSON.stringify({
          title: taskTitle,
          description: taskDesc,
          assigneeEmail: taskAssignee,
          dueDate: taskDueDate || null,
        }),
      });
      setTasks((prev) => [...prev, newTask]);
      setProject((prev) => ({ ...prev, taskCount: prev.taskCount + 1 }));
      setTaskTitle('');
      setTaskDesc('');
      setTaskDueDate('');
      setShowTaskForm(false);
    } catch (err) {
      setTaskError(err.message || 'Could not create task');
    } finally {
      setCreatingTask(false);
    }
  }

  async function handleStatusChange(taskId, newStatus) {
    const prevTasks = tasks;
    setTasks((prev) => prev.map((t) => (t.id === taskId ? { ...t, status: newStatus } : t)));
    try {
      const updated = await apiClient(`/projects/${id}/tasks/${taskId}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status: newStatus }),
      });
      setTasks((prev) => prev.map((t) => (t.id === taskId ? updated : t)));
    } catch (err) {
      setTasks(prevTasks);
      setError(err.message || 'Could not update task status');
    }
  }

  if (loading) {
    return (
      <>
        <Navbar />
        <main className="page"><div className="container"><p className="spinner-text">Loading project…</p></div></main>
      </>
    );
  }

  if (error && !project) {
    return (
      <>
        <Navbar />
        <main className="page"><div className="container">
          <Link to="/dashboard" className="back-link">← Back to dashboard</Link>
          <div className="error-text">{error}</div>
        </div></main>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <main className="page">
        <div className="container">
          <Link to="/dashboard" className="back-link">← Back to dashboard</Link>

          <div className="page-header">
            <div>
              <h1>{project.name}</h1>
              <p>{project.description || 'No description yet.'}</p>
              <div className="project-meta-bar">
                <span>owner: {project.ownerName}</span>
                <span>{project.memberCount} members</span>
                <span>{project.taskCount} tasks</span>
                <span className={`role-badge ${isAdmin ? 'admin' : 'member'}`}>{project.currentUserRole}</span>
              </div>
            </div>
            {isAdmin && (
              <div className="actions-row">
                <button className="btn btn-secondary" onClick={() => setShowMemberForm((v) => !v)}>
                  + Add member
                </button>
                <button className="btn btn-primary" onClick={() => setShowTaskForm((v) => !v)}>
                  + New task
                </button>
              </div>
            )}
          </div>

          {error && <div className="error-text">{error}</div>}

          <span className="section-title">Members</span>
          <div className="members-rail">
            {members.map((m) => (
              <div className="member-chip" key={m.userId}>
                <span className="member-avatar">{m.name.charAt(0)}</span>
                {m.name} {m.role === 'ADMIN' && '· admin'}
              </div>
            ))}
          </div>

          {showMemberForm && (
            <div className="panel">
              <div className="panel-header">
                <h3>Add member</h3>
                <button className="btn btn-ghost btn-sm" onClick={() => setShowMemberForm(false)}>Cancel</button>
              </div>
              {memberError && <div className="error-text">{memberError}</div>}
              <form onSubmit={handleAddMember}>
                <div className="form-row">
                  <div className="field">
                    <label className="label" htmlFor="memail">Email</label>
                    <input
                      id="memail"
                      className="input"
                      type="email"
                      value={memberEmail}
                      onChange={(e) => setMemberEmail(e.target.value)}
                      placeholder="teammate@company.com"
                      required
                    />
                  </div>
                  <div className="field" style={{ maxWidth: 160 }}>
                    <label className="label" htmlFor="mrole">Role</label>
                    <select id="mrole" className="select" value={memberRole} onChange={(e) => setMemberRole(e.target.value)}>
                      <option value="MEMBER">Member</option>
                      <option value="ADMIN">Admin</option>
                    </select>
                  </div>
                </div>
                <button className="btn btn-primary" type="submit" disabled={addingMember}>
                  {addingMember ? 'Adding…' : 'Add to project'}
                </button>
              </form>
            </div>
          )}

          {showTaskForm && (
            <div className="panel">
              <div className="panel-header">
                <h3>New task</h3>
                <button className="btn btn-ghost btn-sm" onClick={() => setShowTaskForm(false)}>Cancel</button>
              </div>
              {taskError && <div className="error-text">{taskError}</div>}
              <form onSubmit={handleCreateTask}>
                <div className="field">
                  <label className="label" htmlFor="ttitle">Title</label>
                  <input
                    id="ttitle"
                    className="input"
                    value={taskTitle}
                    onChange={(e) => setTaskTitle(e.target.value)}
                    required
                  />
                </div>
                <div className="field">
                  <label className="label" htmlFor="tdesc">Description</label>
                  <textarea
                    id="tdesc"
                    className="textarea"
                    value={taskDesc}
                    onChange={(e) => setTaskDesc(e.target.value)}
                  />
                </div>
                <div className="form-row">
                  <div className="field">
                    <label className="label" htmlFor="tassignee">Assignee</label>
                    <select
                      id="tassignee"
                      className="select"
                      value={taskAssignee}
                      onChange={(e) => setTaskAssignee(e.target.value)}
                      required
                    >
                      {members.map((m) => (
                        <option key={m.userId} value={m.email}>{m.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="field">
                    <label className="label" htmlFor="tdue">Due date</label>
                    <input
                      id="tdue"
                      className="input"
                      type="date"
                      value={taskDueDate}
                      onChange={(e) => setTaskDueDate(e.target.value)}
                    />
                  </div>
                </div>
                <button className="btn btn-primary" type="submit" disabled={creatingTask}>
                  {creatingTask ? 'Creating…' : 'Create task'}
                </button>
              </form>
            </div>
          )}

          <span className="section-title" style={{ marginTop: 8 }}>Tasks</span>
          <div className="board">
            {COLUMNS.map((col) => {
              const colTasks = tasks.filter((t) => t.status === col.key);
              return (
                <div className="board-column" key={col.key}>
                  <div className="column-tab">
                    <span className="column-tab-label">
                      <span className={`column-dot ${col.dot}`} aria-hidden="true"></span>
                      {col.label}
                    </span>
                    <span className="column-count">{colTasks.length}</span>
                  </div>
                  <div className="task-list">
                    {colTasks.map((task) => {
                      const canUpdateStatus = isAdmin || task.assigneeEmail === user?.email;
                      return (
                        <TaskCard
                          key={task.id}
                          task={task}
                          canUpdateStatus={canUpdateStatus}
                          onStatusChange={handleStatusChange}
                        />
                      );
                    })}
                    {colTasks.length === 0 && (
                      <p style={{ color: 'var(--ink-faint)', fontSize: '0.82rem' }}>Nothing here.</p>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </main>
    </>
  );
}
