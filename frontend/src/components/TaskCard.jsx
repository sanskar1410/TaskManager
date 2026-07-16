const STATUS_META = {
  TODO: { label: 'To do', className: 'todo' },
  IN_PROGRESS: { label: 'In progress', className: 'progress' },
  DONE: { label: 'Done', className: 'done' },
};

function formatDate(dateStr) {
  if (!dateStr) return null;
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

export default function TaskCard({ task, canUpdateStatus, onStatusChange }) {
  const meta = STATUS_META[task.status] || STATUS_META.TODO;
  const statusClass = task.status === 'IN_PROGRESS' ? 'status-progress'
    : task.status === 'DONE' ? 'status-done' : 'status-todo';

  return (
    <div className={`index-card task-card ${statusClass} ${task.overdue ? 'overdue' : ''}`}>
      {task.overdue && <div className="task-card-overdue-flag" title="Overdue" aria-hidden="true"></div>}

      <div className="task-title">{task.title}</div>
      {task.description && <div className="task-desc">{task.description}</div>}

      <span className={`status-stamp ${meta.className}`}>{meta.label}</span>

      <div className="task-card-footer">
        <span className="task-assignee" title={task.assigneeEmail}>{task.assigneeName}</span>
        {task.dueDate && (
          <span className={`task-due ${task.overdue ? 'is-overdue' : ''}`}>
            {task.overdue ? 'overdue · ' : 'due '}{formatDate(task.dueDate)}
          </span>
        )}
      </div>

      {canUpdateStatus && (
        <select
          className="status-select"
          value={task.status}
          onChange={(e) => onStatusChange(task.id, e.target.value)}
          aria-label={`Update status for ${task.title}`}
        >
          <option value="TODO">To do</option>
          <option value="IN_PROGRESS">In progress</option>
          <option value="DONE">Done</option>
        </select>
      )}
    </div>
  );
}
