import React from "react";
import { useParams } from "react-router-dom";

function NoteDetails({ notes }) {
  const { index } = useParams();
  const note = notes[index];

  if (!note) {
    return <p>Failed to load notes</p>;
  }

  return (
    <div>
      <p>Note Name: {Note.name}</p>
      <p>Note Time: {displayTimeOrDate}</p>
      <img src={Note.image} alt={`Note-Picture-${index}`} />
    </div>
  );
}

export default NoteDetails;
