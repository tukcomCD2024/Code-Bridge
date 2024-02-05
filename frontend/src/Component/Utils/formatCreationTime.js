export function formatCreationTime(submissionTime) {
  const submissionDate = new Date(submissionTime);
  const today = new Date();

  if (
    submissionDate.getFullYear() === today.getFullYear() &&
    submissionDate.getMonth() === today.getMonth() &&
    submissionDate.getDate() === today.getDate()
  ) {
    return submissionDate.toLocaleTimeString();
  } else {
    return submissionDate.toLocaleDateString();
  }
}
