export function RuleInput({ label, hint, value, onChange }) {
  return (
    <label className="rule-field">
      <span>{label}</span>
      <textarea rows="2" placeholder={hint} value={value}
        onChange={(event) => onChange(event.target.value)} />
    </label>
  )
}
