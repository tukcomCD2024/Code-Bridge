import React from "react";
import { useParams } from "react-router-dom";

function OrganizationDetails({ organizations }) {
  const { index } = useParams();
  const organization = organizations[index];

  if (!organization) {
    return <p>Failed to load organization</p>;
  }

  return (
    <div>
      <p>Organization Name: {organization.name}</p>
      <p>Submission Time: {displayTimeOrDate}</p>
      <img src={organization.image} alt={`Organization-Picture-${index}`} />
    </div>
  );
}

export default OrganizationDetails;
