import { useNavigate } from 'react-router-dom';

export default function ProjectCard({ project }) {
  const navigate = useNavigate();
  const isAdmin = project.currentUserRole === 'ADMIN';

  return (
    <div
      className="index-card project-card"
      onClick={() => navigate(`/projects/${project.id}`)}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/projects/${project.id}`); }}
    >
      <div className="project-card-top">
        <h3>{project.name}</h3>
        <span className={`role-badge ${isAdmin ? 'admin' : 'member'}`}>
          {project.currentUserRole}
        </span>
      </div>
      <p className="project-card-desc">{project.description || 'No description yet.'}</p>
      <div className="project-card-meta">
        <span>{project.memberCount} member{project.memberCount === 1 ? '' : 's'}</span>
        <span>{project.taskCount} task{project.taskCount === 1 ? '' : 's'}</span>
        <span>owner: {project.ownerName}</span>
      </div>
    </div>
  );
}
