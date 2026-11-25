import { useId } from 'react'

export function RuleInput({ label, hint, value, onChange }) {
  const inputId = useId()

  return (
    <div className="rule-field">
      <label htmlFor={inputId}>{label}</label>
      <textarea id={inputId} aria-describedby={`${inputId}-hint`} rows="2" value={value}
        onChange={(event) => onChange(event.target.value)} />
      <small id={`${inputId}-hint`}>{hint}</small>
    </div>
  )
}
