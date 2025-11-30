import Image from "next/image"

interface StepCardProps {
  step: {
    number: string
    title: string
    description: string
    color: string
    image: string
  }
}

export default function StepCard({ step }: StepCardProps) {
  return (
    <div className="bg-white/90 dark:bg-gray-900/80 backdrop-blur-sm border border-slate-200 dark:border-gray-800 rounded-xl overflow-hidden shadow-lg hover:shadow-[0_20px_45px_rgba(24,226,153,0.15)] hover:-translate-y-1 transition-all duration-300">
      {/* Image at top */}
      <div className="relative h-40 overflow-hidden">
        <Image
          src={step.image || "/placeholder.svg"}
          alt={step.title}
          fill
          className="object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-gray-900 to-transparent"></div>
        
        {/* Step number overlay */}
        <div className="absolute top-3 right-3 bg-white/80 dark:bg-gray-900/70 backdrop-blur-sm rounded-full h-8 w-8 flex items-center justify-center border border-slate-200 dark:border-gray-700">
          <span className="text-sm font-bold text-slate-900 dark:text-white">{step.number}</span>
        </div>
      </div>
      
      {/* Content */}
      <div className="p-5">
        <h3 className="text-xl font-bold mb-2 text-slate-900 dark:bg-gradient-to-r dark:from-white dark:to-gray-300 dark:bg-clip-text dark:text-transparent">
          {step.title}
        </h3>
        <p className="text-slate-600 dark:text-gray-400 text-sm">
          {step.description}
        </p>
      </div>
    </div>
  )
}
