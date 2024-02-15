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
      <p>Note Name: </p>
      <p>Note Time: </p>

    </div>
  );
}

export default NoteDetails;
