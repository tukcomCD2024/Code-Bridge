import React from "react";
import { useParams } from "react-router-dom";

function OrganizationDetails({ organizations }) {
  const { index } = useParams();
  const organization = organizations[index];

  if (!organization) {
    return <p>Organization not connected</p>;
  }

  const submissionDate = new Date(organization.submissionTime);
  const today = new Date();

  let displayTimeOrDate;

  if (
    submissionDate.getFullYear() === today.getFullYear() &&
    submissionDate.getMonth() === today.getMonth() &&
    submissionDate.getDate() === today.getDate()
  ) {
    // If the submission time is today, display only the time
    displayTimeOrDate = submissionDate.toLocaleTimeString();
  } else {
    // If the submission time is not today, display only the date
    displayTimeOrDate = submissionDate.toLocaleDateString();
  }

  return (
    <div>
      <p>Organization Name: {organization.name}</p>
      <p>Submission Time: {displayTimeOrDate}</p>
      <img src={organization.image} alt={`Organization-Picture-${index}`} />
      {/* Add more details as needed */}
    </div>
  );
}

export default OrganizationDetails;
